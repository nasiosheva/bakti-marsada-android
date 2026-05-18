package com.lampung.baktimarsada.feature.arisan.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.ArisanGroup
import com.lampung.baktimarsada.domain.model.ArisanParticipant
import com.lampung.baktimarsada.domain.model.ArisanPeriod
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.domain.model.PaymentStatus
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.repository.MemberRepository
import com.lampung.baktimarsada.repository.PaymentObligationRepository
import com.lampung.baktimarsada.ui.component.BaktiBottomSheet
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.JemaatPill
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun ArisanRoute(
    isAdmin: Boolean,
    session: SessionState,
    bottomContentPadding: Dp = 0.dp,
    viewModel: ArisanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(session.sectorContext.sectorId) {
        viewModel.refresh(session.sectorContext)
    }

    ArisanContent(
        isAdmin = isAdmin,
        session = session,
        state = state,
        bottomContentPadding = bottomContentPadding,
        onGeneratePeriod = { viewModel.generatePeriod(session.sectorContext) },
        onDrawWinner = viewModel::drawWinner
    )
}

data class ArisanUiState(
    val group: ArisanGroup? = null,
    val isLoading: Boolean = true,
    val message: String? = null,
    val winnerName: String? = null
)

@HiltViewModel
class ArisanViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    private val paymentRepository: PaymentObligationRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {
    private val _state = MutableStateFlow(ArisanUiState())
    val state: StateFlow<ArisanUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                memberRepository.observeMembers(),
                paymentRepository.observeObligations()
            ) { members, obligations ->
                buildArisanGroup(members, obligations)
            }.collect { group ->
                _state.update { it.copy(group = group, isLoading = false) }
            }
        }
    }

    fun refresh(sectorContext: SectorContext) {
        viewModelScope.launch(dispatcherProvider.io) {
            memberRepository.refresh(sectorContext)
            paymentRepository.refresh(sectorContext)
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

    fun drawWinner() {
        val participants = _state.value.group?.participants.orEmpty()
        val previousWinnerName = _state.value.winnerName
        val candidates = participants
            .filter { it.hasPaid }
            .ifEmpty { participants }
        val winnerPool = candidates
            .filterNot { it.memberName == previousWinnerName }
            .ifEmpty { candidates }
        val winner = winnerPool.randomOrNull()
        _state.update { state ->
            state.copy(
                winnerName = winner?.memberName,
                message = winner?.let { "Pemenang arisan: ${it.memberName}" } ?: "Belum ada peserta arisan."
            )
        }
    }

    private fun buildArisanGroup(
        members: List<MemberDetail>,
        obligations: List<PaymentObligationDetail>
    ): ArisanGroup {
        val arisanPayments = obligations.filter { it.title.contains(ARISAN_TITLE_KEYWORD, ignoreCase = true) }
        val paymentsByMember = arisanPayments.associateBy { it.memberId }
        val participants = members.map { member ->
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

@Composable
private fun ArisanContent(
    isAdmin: Boolean,
    session: SessionState,
    state: ArisanUiState,
    bottomContentPadding: Dp,
    onGeneratePeriod: () -> Unit,
    onDrawWinner: () -> Unit
) {
    val group = state.group
    val participants = group?.participants.orEmpty()
    val paidCount = participants.count { it.hasPaid }
    val ownParticipant = participants.firstOrNull { it.memberName.equals(session.displayName, ignoreCase = true) }
    var showRedrawConfirmation by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                ),
            contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = bottomContentPadding + 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ArisanHeroCard(
                    title = group?.title ?: stringResource(id = R.string.arisan_title),
                    sectorName = session.sectorContext.sectorName,
                    periodLabel = group?.activePeriod?.label ?: "-",
                    amount = group?.contributionAmount ?: 0L
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ArisanMetricCard(
                        icon = Icons.Filled.Groups,
                        label = stringResource(id = R.string.arisan_participant_count),
                        value = participants.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    ArisanMetricCard(
                        icon = Icons.Filled.Payments,
                        label = stringResource(id = R.string.arisan_paid_count),
                        value = "$paidCount/${participants.size}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            state.message?.let { message ->
                item { BaktiSectionMessage(message = message) }
            }
            if (isAdmin) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onGeneratePeriod,
                            enabled = participants.isNotEmpty() && !state.isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(text = stringResource(id = R.string.arisan_action_generate_dues))
                        }
                        OutlinedButton(
                            onClick = {
                                if (state.winnerName == null) {
                                    onDrawWinner()
                                } else {
                                    showRedrawConfirmation = true
                                }
                            },
                            enabled = participants.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Shuffle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(id = R.string.arisan_action_draw_winner))
                        }
                    }
                }
            } else {
                item {
                    ArisanOwnStatusCard(participant = ownParticipant)
                }
            }
            item {
                Text(
                    text = stringResource(id = R.string.arisan_schedule_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                ArisanPeriodCard(
                    period = group?.activePeriod,
                    winnerName = state.winnerName
                )
            }
            item {
                Text(
                    text = stringResource(id = R.string.arisan_participants_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (participants.isEmpty()) {
                item {
                    ArisanEmptyCard(message = stringResource(id = R.string.arisan_empty_participants))
                }
            } else {
                items(participants, key = { it.memberId }) { participant ->
                    ArisanParticipantCard(
                        participant = participant,
                        showPaymentStatus = isAdmin || participant.memberName.equals(session.displayName, ignoreCase = true)
                    )
                }
            }
        }

        if (showRedrawConfirmation) {
            ArisanRedrawConfirmationSheet(
                previousWinnerName = state.winnerName.orEmpty(),
                onDismiss = { showRedrawConfirmation = false },
                onConfirm = {
                    showRedrawConfirmation = false
                    onDrawWinner()
                }
            )
        }
    }
}

@Composable
private fun ArisanRedrawConfirmationSheet(
    previousWinnerName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    BaktiBottomSheet(
        onDismissRequest = onDismiss,
        title = stringResource(id = R.string.arisan_redraw_confirm_title)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(id = R.string.arisan_redraw_confirm_message, previousWinnerName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = stringResource(id = R.string.action_cancel))
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = stringResource(id = R.string.arisan_action_redraw))
                }
            }
        }
    }
}

@Composable
private fun ArisanHeroCard(
    title: String,
    sectorName: String,
    periodLabel: String,
    amount: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JemaatPill(
                    text = sectorName,
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
                JemaatPill(
                    text = periodLabel,
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = stringResource(id = R.string.arisan_contribution_amount, amount.toRupiahLabel()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
            )
        }
    }
}

@Composable
private fun ArisanMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ArisanOwnStatusCard(participant: ArisanParticipant?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(id = R.string.arisan_my_due_status),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = participant?.paymentStatus?.toPaymentStatusLabel() ?: stringResource(id = R.string.arisan_not_registered),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ArisanPeriodCard(
    period: ArisanPeriod?,
    winnerName: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = period?.label ?: "-",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.arisan_due_date, period?.dueDate ?: "-"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(id = R.string.arisan_winner, winnerName ?: period?.winnerMemberName ?: "-"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ArisanParticipantCard(
    participant: ArisanParticipant,
    showPaymentStatus: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Filled.Wallet, contentDescription = null)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(participant.memberName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (showPaymentStatus) {
                    Text(
                        text = participant.paymentStatus?.toPaymentStatusLabel() ?: stringResource(id = R.string.arisan_due_not_generated),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ArisanEmptyCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PaymentStatus.toPaymentStatusLabel(): String {
    return when (this) {
        PaymentStatus.UNPAID -> stringResource(id = R.string.payment_status_unpaid)
        PaymentStatus.PAID -> stringResource(id = R.string.payment_status_paid)
        PaymentStatus.OVERDUE -> stringResource(id = R.string.payment_status_overdue)
    }
}

private fun Long.toRupiahLabel(): String {
    return "Rp" + toString().reversed().chunked(3).joinToString(".").reversed()
}

@Preview(name = "Arisan Admin")
@Composable
private fun ArisanAdminPreview() {
    BaktiMarsadaTheme {
        ArisanContent(
            isAdmin = true,
            session = previewArisanSession,
            state = previewArisanState,
            bottomContentPadding = 0.dp,
            onGeneratePeriod = {},
            onDrawWinner = {}
        )
    }
}

@Preview(name = "Arisan Jemaat")
@Composable
private fun ArisanJemaatPreview() {
    BaktiMarsadaTheme {
        ArisanContent(
            isAdmin = false,
            session = previewArisanSession.copy(role = UserRole.JEMAAT, displayName = "P. Simanjuntak"),
            state = previewArisanState,
            bottomContentPadding = 0.dp,
            onGeneratePeriod = {},
            onDrawWinner = {}
        )
    }
}

private val previewArisanSession = SessionState(
    authToken = "preview",
    userId = "u-1",
    displayName = "Admin Sektor",
    role = UserRole.ADMIN,
    tenantContext = com.lampung.baktimarsada.domain.model.TenantContext(
        tenantId = "hkbp",
        tenantName = "HKBP",
        subTenantId = "kedaton",
        subTenantName = "HKBP Kedaton"
    ),
    sectorContext = SectorContext(sectorId = "sector-1", sectorName = "Sektor 1")
)

private val previewArisanState = ArisanUiState(
    group = ArisanGroup(
        id = "arisan-sector-1",
        title = "Arisan Sektor 1",
        sectorId = "sector-1",
        sectorName = "Sektor 1",
        contributionAmount = 100_000,
        activePeriod = ArisanPeriod(
            id = "period-1",
            label = "Mei 2026",
            contributionAmount = 100_000,
            dueDate = "2026-05-30",
            winnerMemberId = null,
            winnerMemberName = null
        ),
        participants = listOf(
            ArisanParticipant("member-1", "P. Simanjuntak", PaymentStatus.PAID),
            ArisanParticipant("member-2", "S. Sihombing", PaymentStatus.UNPAID),
            ArisanParticipant("member-3", "R. Naibaho", null)
        )
    ),
    isLoading = false,
    winnerName = "P. Simanjuntak"
)

// created by Mories Deo Hutapea, S.E.,S.Kom
