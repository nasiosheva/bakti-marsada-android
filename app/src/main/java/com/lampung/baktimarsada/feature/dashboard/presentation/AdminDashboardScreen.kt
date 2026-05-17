package com.lampung.baktimarsada.feature.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppBuildConfig
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.repository.AuthRepository
import com.lampung.baktimarsada.domain.repository.EventRepository
import com.lampung.baktimarsada.domain.repository.FinanceReportRepository
import com.lampung.baktimarsada.domain.repository.MemberRepository
import com.lampung.baktimarsada.domain.repository.PaymentObligationRepository
import com.lampung.baktimarsada.ui.component.BaktiLoadingState
import com.lampung.baktimarsada.ui.component.BaktiPullToRefreshBox
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.BaktiScrollableStateView
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardRoute(
    session: SessionState,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val cards = listOf(
        DashboardCardUi(
            title = stringResource(id = R.string.dashboard_events_title),
            value = state.cards.eventCount.toString(),
            description = stringResource(id = R.string.dashboard_events_desc)
        ),
        DashboardCardUi(
            title = stringResource(id = R.string.dashboard_members_title),
            value = state.cards.memberCount.toString(),
            description = stringResource(id = R.string.dashboard_members_desc)
        ),
        DashboardCardUi(
            title = stringResource(id = R.string.dashboard_finance_title),
            value = state.cards.financeCount.toString(),
            description = stringResource(id = R.string.dashboard_finance_desc)
        ),
        DashboardCardUi(
            title = stringResource(id = R.string.dashboard_payments_title),
            value = state.cards.paymentCount.toString(),
            description = stringResource(id = R.string.dashboard_payments_desc)
        )
    )

    BaktiPullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = viewModel::refreshAll,
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (state.isLoading && state.cards == DashboardCounts()) {
            BaktiScrollableStateView { BaktiLoadingState() }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.admin_dashboard_title, session.sectorContext.sectorName),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                state.errorMessage?.let { message ->
                    item { BaktiSectionMessage(message = message) }
                }
                if (AppBuildConfig.simulationEnabled) {
                    item {
                        OutlinedButton(
                            onClick = viewModel::resetSimulationData,
                            enabled = !state.isLoading
                        ) {
                            Text(text = stringResource(id = R.string.dashboard_simulation_reset_action))
                        }
                    }
                }
                items(cards) { card ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = card.title, style = MaterialTheme.typography.titleMedium)
                            Text(text = card.value, style = MaterialTheme.typography.headlineSmall)
                            Text(text = card.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

data class DashboardCardUi(
    val title: String,
    val value: String,
    val description: String
)

data class DashboardCounts(
    val eventCount: Int = 0,
    val memberCount: Int = 0,
    val financeCount: Int = 0,
    val paymentCount: Int = 0
)

data class AdminDashboardUiState(
    val cards: DashboardCounts = DashboardCounts(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

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
