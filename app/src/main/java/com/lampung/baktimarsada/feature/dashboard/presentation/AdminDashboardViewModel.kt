package com.lampung.baktimarsada.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.repository.AuthRepository
import com.lampung.baktimarsada.repository.EventRepository
import com.lampung.baktimarsada.repository.FinanceReportRepository
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

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository,
    private val memberRepository: MemberRepository,
    private val financeReportRepository: FinanceReportRepository,
    private val paymentObligationRepository: PaymentObligationRepository,
    private val remoteDataSource: AppRemoteDataSource,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _state = MutableStateFlow(AdminDashboardUiState())
    val state: StateFlow<AdminDashboardUiState> = _state.asStateFlow()

    init {
        observeCounts()
        refreshAll()
    }

    private fun observeCounts() {
        viewModelScope.launch(dispatcherProvider.io) {
            combine(
                eventRepository.observeEvents(),
                memberRepository.observeMembers(),
                financeReportRepository.observeReports(),
                paymentObligationRepository.observeObligations()
            ) { events, members, reports, obligations ->
                DashboardCounts(
                    eventCount = events.size,
                    memberCount = members.size,
                    financeCount = reports.size,
                    paymentCount = obligations.size
                )
            }.collect { counts ->
                _state.update {
                    it.copy(
                        cards = counts,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch(dispatcherProvider.io) {
            val session = authRepository.getCurrentSession() ?: return@launch
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val results = listOf(
                eventRepository.refresh(session.sectorContext),
                memberRepository.refresh(session.sectorContext),
                financeReportRepository.refresh(session.sectorContext),
                paymentObligationRepository.refresh(session.sectorContext)
            )
            val firstError = results.filterIsInstance<AppResult.Error>().firstOrNull()
            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessage = firstError?.message
                )
            }
        }
    }

    fun resetSimulationData() {
        viewModelScope.launch(dispatcherProvider.io) {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                remoteDataSource.resetSimulationData()
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to reset simulation data"
                    )
                }
                return@launch
            }
            refreshAll()
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
