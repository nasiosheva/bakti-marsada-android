package com.lampung.baktimarsada.feature.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppBuildConfig
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.ui.component.BaktiLoadingState
import com.lampung.baktimarsada.ui.component.BaktiPullToRefreshBox
import com.lampung.baktimarsada.ui.component.BaktiScrollableStateView
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardRoute(
    session: SessionState,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AdminDashboardContent(
        state = state,
        onRefresh = viewModel::refreshAll,
        onResetSimulation = viewModel::resetSimulationData
    )
}

@Composable
fun AdminDashboardContent(
    state: AdminDashboardUiState,
    onRefresh: () -> Unit,
    onResetSimulation: () -> Unit
) {
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
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (state.isLoading && state.cards == DashboardCounts()) {
            BaktiScrollableStateView { BaktiLoadingState() }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.errorMessage?.let { message ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        BaktiSectionMessage(message = message)
                    }
                }
                if (AppBuildConfig.simulationEnabled) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        OutlinedButton(
                            onClick = onResetSimulation,
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth()
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

@Preview(name = "Dashboard - filled")
@Composable
private fun AdminDashboardContentPreview() {
    val sampleState = AdminDashboardUiState(
        cards = DashboardCounts(
            eventCount = 12,
            memberCount = 84,
            financeCount = 5,
            paymentCount = 3
        ),
        isLoading = false
    )

    BaktiMarsadaTheme {
        AdminDashboardContent(
            state = sampleState,
            onRefresh = {},
            onResetSimulation = {}
        )
    }
}

@Preview(name = "Dashboard - loading")
@Composable
private fun AdminDashboardContentLoadingPreview() {
    BaktiMarsadaTheme {
        AdminDashboardContent(
            state = AdminDashboardUiState(isLoading = true),
            onRefresh = {},
            onResetSimulation = {}
        )
    }
}

@Preview(name = "Dashboard - error")
@Composable
private fun AdminDashboardContentErrorPreview() {
    BaktiMarsadaTheme {
        AdminDashboardContent(
            state = AdminDashboardUiState(
                cards = DashboardCounts(
                    eventCount = 2,
                    memberCount = 5,
                    financeCount = 1,
                    paymentCount = 4
                ),
                isLoading = false,
                errorMessage = "Gagal memuat data ringkasan"
            ),
            onRefresh = {},
            onResetSimulation = {}
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
