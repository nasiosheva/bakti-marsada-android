package com.lampung.baktimarsada.feature.finance.presentation

import com.lampung.baktimarsada.model.FinanceReportDetail

data class FinanceUiState(
    val items: List<FinanceReportDetail> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

sealed interface FinanceEvent {
    data object Refresh : FinanceEvent
    data class Save(val item: FinanceReportDetail) : FinanceEvent
    data class Delete(val id: String) : FinanceEvent
    data class ToggleVisibility(val item: FinanceReportDetail) : FinanceEvent
}

// created by Mories Deo Hutapea, S.E.,S.Kom
