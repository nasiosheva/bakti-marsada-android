package com.lampung.baktimarsada.feature.arisan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.ArisanGroup
import com.lampung.baktimarsada.domain.model.ArisanParticipant
import com.lampung.baktimarsada.domain.model.ArisanPeriod
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.domain.model.PaymentStatus
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.repository.ArisanParticipantRepository
import com.lampung.baktimarsada.repository.MemberRepository
import com.lampung.baktimarsada.repository.PaymentObligationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArisanUiState(
    val group: ArisanGroup? = null,
    val allMembers: List<MemberDetail> = emptyList(),
    val selectedParticipantIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val message: String? = null,
    val winnerName: String? = null
)

@HiltViewModel
class ArisanViewModel @Inject constructor(
    private val arisanParticipantRepository: ArisanParticipantRepository,
    private val memberRepository: MemberRepository,
    private val paymentRepository: PaymentObligationRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {
    private val _state = MutableStateFlow(ArisanUiState())
    val state: StateFlow<ArisanUiState> = _state.asStateFlow()
    private val selectedIdsFlow = MutableStateFlow<Set<String>>(emptySet())

    init {
        viewModelScope.launch {
            combine(
                memberRepository.observeMembers(),
                paymentRepository.observeObligations(),
                selectedIdsFlow
            ) { members, obligations, selectedParticipantIds ->
                Triple(
                    buildArisanGroup(members, obligations, selectedParticipantIds),
                    members,
                    selectedParticipantIds
                )
            }.collect { group ->
                _state.update {
                    it.copy(
                        group = group.first,
                        allMembers = group.second,
                        selectedParticipantIds = group.third,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refresh(sectorContext: SectorContext) {
        viewModelScope.launch(dispatcherProvider.io) {
            _state.update { it.copy(isLoading = true) }
            memberRepository.refresh(sectorContext)
            paymentRepository.refresh(sectorContext)
            when (val result = arisanParticipantRepository.fetch(sectorContext)) {
                is AppResult.Success -> {
                    selectedIdsFlow.value = result.data.toSet()
                    _state.update { it.copy(message = null, isLoading = false) }
                }
                is AppResult.Error -> {
                    selectedIdsFlow.value = emptySet()
                    _state.update {
                        it.copy(
                            message = result.message,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun fillAllParticipants(sectorContext: SectorContext) {
        viewModelScope.launch(dispatcherProvider.io) {
            _state.update { it.copy(isLoading = true, message = null) }
            when (val result = arisanParticipantRepository.fillAllFromMembers(sectorContext)) {
                is AppResult.Success -> {
                    selectedIdsFlow.value = result.data.toSet()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "Peserta arisan berhasil diisi dari semua anggota."
                        )
                    }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false, message = result.message) }
                }
            }
        }
    }

    fun saveSelectedParticipants(sectorContext: SectorContext, memberIds: Set<String>) {
        viewModelScope.launch(dispatcherProvider.io) {
            _state.update { it.copy(isLoading = true, message = null) }
            when (val result = arisanParticipantRepository.replace(sectorContext, memberIds.toList())) {
                is AppResult.Success -> {
                    selectedIdsFlow.value = result.data.toSet()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "Peserta arisan berhasil diperbarui."
                        )
                    }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false, message = result.message) }
                }
            }
        }
    }

    fun generatePeriod(sectorContext: SectorContext) {
        val group = _state.value.group ?: return
        viewModelScope.launch(dispatcherProvider.io) {
            _state.update { it.copy(isLoading = true, message = null) }
            var failureMessage: String? = null
            group.participants.forEach { participant ->
                val result = paymentRepository.save(
                    PaymentObligationDetail(
                        id = "",
                        memberId = participant.memberId,
                        memberName = participant.memberName,
                        title = ARISAN_PAYMENT_TITLE,
                        description = "Iuran arisan sektor untuk periode $ARISAN_PERIOD_LABEL.",
                        amount = ARISAN_CONTRIBUTION_AMOUNT,
                        dueDate = ARISAN_DUE_DATE,
                        status = PaymentStatus.UNPAID,
                        sectorId = sectorContext.sectorId,
                        sectorName = sectorContext.sectorName
                    ),
                    sectorContext
                )
                if (result is AppResult.Error) {
                    failureMessage = result.message
                }
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    message = failureMessage ?: "Tagihan arisan berhasil dibuat untuk peserta."
                )
            }
            refresh(sectorContext)
        }
    }

    fun setWinner(winnerName: String) {
        _state.update { state ->
            state.copy(
                winnerName = winnerName.takeIf { it.isNotBlank() },
                message = winnerName.takeIf { it.isNotBlank() }
                    ?.let { "Pemenang arisan: $it" }
                    ?: "Belum ada peserta arisan."
            )
        }
    }

    private fun buildArisanGroup(
        members: List<MemberDetail>,
        obligations: List<PaymentObligationDetail>,
        selectedParticipantIds: Set<String>
    ): ArisanGroup {
        val arisanPayments = obligations.filter { it.title.contains(ARISAN_TITLE_KEYWORD, ignoreCase = true) }
        val paymentsByMember = arisanPayments.associateBy { it.memberId }
        val selectedMembers = members.filter { it.id in selectedParticipantIds }
        val participants = selectedMembers.map { member ->
            ArisanParticipant(
                memberId = member.id,
                memberName = member.fullName,
                paymentStatus = paymentsByMember[member.id]?.status
            )
        }
        val firstMember = members.firstOrNull()
        return ArisanGroup(
            id = "arisan-${firstMember?.sectorId.orEmpty()}",
            title = "Arisan ${firstMember?.sectorName ?: "Sektor"}",
            sectorId = firstMember?.sectorId.orEmpty(),
            sectorName = firstMember?.sectorName.orEmpty(),
            contributionAmount = ARISAN_CONTRIBUTION_AMOUNT,
            activePeriod = ArisanPeriod(
                id = "arisan-period-$ARISAN_PERIOD_LABEL",
                label = ARISAN_PERIOD_LABEL,
                contributionAmount = ARISAN_CONTRIBUTION_AMOUNT,
                dueDate = ARISAN_DUE_DATE,
                winnerMemberId = null,
                winnerMemberName = _state.value.winnerName
            ),
            participants = participants
        )
    }

    companion object {
        private const val ARISAN_TITLE_KEYWORD = "Arisan"
        private const val ARISAN_PAYMENT_TITLE = "Iuran Arisan Mei 2026"
        private const val ARISAN_PERIOD_LABEL = "Mei 2026"
        private const val ARISAN_DUE_DATE = "2026-05-30"
        private const val ARISAN_CONTRIBUTION_AMOUNT = 100_000L
    }
}

internal fun ArisanUiState.buildWinnerCandidates(): List<ArisanParticipant> {
    val participants = group?.participants.orEmpty()
    val candidates = participants
        .filter { it.hasPaid }
        .ifEmpty { participants }
    return candidates
        .filterNot { it.memberName == winnerName }
        .ifEmpty { candidates }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
