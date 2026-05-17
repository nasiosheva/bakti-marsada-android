package com.lampung.baktimarsada.feature.dashboard.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

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

// created by Mories Deo Hutapea, S.E.,S.Kom
