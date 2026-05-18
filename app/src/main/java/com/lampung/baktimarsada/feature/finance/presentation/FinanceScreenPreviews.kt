package com.lampung.baktimarsada.feature.finance.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lampung.baktimarsada.model.FinanceReportDetail
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

private val financeItems = listOf(
    FinanceReportDetail(
        id = "f-1",
        title = "Iuran Operasional",
        description = "Laporan bulanan",
        periodLabel = "Mei 2026",
        amount = 500000,
        isVisibleToJemaat = true,
        sectorId = "sec-1",
        sectorName = "Lingkungan 1"
    )
)

@Preview(name = "Finance - admin")
@Composable
private fun FinanceContentAdminPreview() {
    BaktiMarsadaTheme {
        FinanceContent(
            isAdmin = true,
            state = FinanceUiState(items = financeItems, isLoading = false),
            onRefresh = {},
            onDelete = {},
            onToggleVisibility = {},
            onShowCreate = {},
            onShowDetail = {},
            onShowEdit = {}
        )
    }
}

@Preview(name = "Finance - jemaat")
@Composable
private fun FinanceContentJemaatPreview() {
    BaktiMarsadaTheme {
        FinanceContent(
            isAdmin = false,
            state = FinanceUiState(items = financeItems, isLoading = false),
            onRefresh = {},
            onDelete = {},
            onToggleVisibility = {},
            onShowCreate = {},
            onShowDetail = {},
            onShowEdit = {}
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
