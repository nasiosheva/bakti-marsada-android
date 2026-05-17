package com.lampung.baktimarsada.feature.dashboard.presentation

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

// created by Mories Deo Hutapea, S.E.,S.Kom
