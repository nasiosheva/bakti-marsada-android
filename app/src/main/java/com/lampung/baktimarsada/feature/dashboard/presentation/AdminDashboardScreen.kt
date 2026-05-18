package com.lampung.baktimarsada.feature.dashboard.presentation

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        DashboardStat(
            title = stringResource(id = R.string.dashboard_events_title),
            value = state.cards.eventCount,
            description = stringResource(id = R.string.dashboard_events_desc),
            icon = Icons.Filled.Event,
            tone = DashboardTone.Primary
        ),
        DashboardStat(
            title = stringResource(id = R.string.dashboard_members_title),
            value = state.cards.memberCount,
            description = stringResource(id = R.string.dashboard_members_desc),
            icon = Icons.Filled.Groups,
            tone = DashboardTone.Tertiary
        ),
        DashboardStat(
            title = stringResource(id = R.string.dashboard_finance_title),
            value = state.cards.financeCount,
            description = stringResource(id = R.string.dashboard_finance_desc),
            icon = Icons.Filled.AccountBalanceWallet,
            tone = DashboardTone.Secondary
        ),
        DashboardStat(
            title = stringResource(id = R.string.dashboard_payments_title),
            value = state.cards.paymentCount,
            description = stringResource(id = R.string.dashboard_payments_desc),
            icon = Icons.Filled.Payments,
            tone = DashboardTone.Error
        )
    )

    BaktiPullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            if (state.isLoading && state.cards == DashboardCounts()) {
                BaktiScrollableStateView { BaktiLoadingState() }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        DashboardSummaryHeader(
                            totalRecords = state.cards.totalRecords()
                        )
                    }
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
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RestartAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.dashboard_simulation_reset_action),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    cards.forEach { stat ->
                        item {
                            DashboardStatCard(stat = stat)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardSummaryHeader(totalRecords: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.dashboard_summary_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = totalRecords.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(id = R.string.dashboard_summary_caption),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                    )
                }
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(stat: DashboardStat) {
    val tone = stat.tone.resolve()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(tone.accent, tone.accent.copy(alpha = 0.55f))
                        )
                    )
            ) {
                Spacer(modifier = Modifier.height(140.dp))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = tone.container,
                        contentColor = tone.onContainer
                    ) {
                        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Icon(
                                imageVector = stat.icon,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Text(
                        text = stat.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = stat.value.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = tone.accent
                )
                Text(
                    text = stat.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class DashboardStat(
    val title: String,
    val value: Int,
    val description: String,
    val icon: ImageVector,
    val tone: DashboardTone
)

private enum class DashboardTone { Primary, Tertiary, Secondary, Error }

private data class ResolvedTone(
    val accent: Color,
    val container: Color,
    val onContainer: Color
)

@Composable
private fun DashboardTone.resolve(): ResolvedTone {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        DashboardTone.Primary -> ResolvedTone(scheme.primary, scheme.primaryContainer, scheme.onPrimaryContainer)
        DashboardTone.Tertiary -> ResolvedTone(scheme.tertiary, scheme.tertiaryContainer, scheme.onTertiaryContainer)
        DashboardTone.Secondary -> ResolvedTone(scheme.secondary, scheme.secondaryContainer, scheme.onSecondaryContainer)
        DashboardTone.Error -> ResolvedTone(scheme.error, scheme.errorContainer, scheme.onErrorContainer)
    }
}

private fun DashboardCounts.totalRecords(): Int =
    eventCount + memberCount + financeCount + paymentCount

@Preview(name = "Dashboard - Filled", showBackground = true, widthDp = 412, heightDp = 720)
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

@Preview(
    name = "Dashboard - Filled Dark",
    showBackground = true,
    widthDp = 412,
    heightDp = 720,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AdminDashboardContentDarkPreview() {
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

@Preview(name = "Dashboard - Loading", showBackground = true, widthDp = 412)
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

@Preview(name = "Dashboard - Error", showBackground = true, widthDp = 412, heightDp = 720)
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

@Preview(name = "DashboardSummaryHeader", showBackground = true, widthDp = 412)
@Composable
private fun DashboardSummaryHeaderPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DashboardSummaryHeader(totalRecords = 104)
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
