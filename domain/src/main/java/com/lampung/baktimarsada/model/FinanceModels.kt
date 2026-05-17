package com.lampung.baktimarsada.domain.model

data class FinanceReportSummary(
    val id: String,
    val title: String,
    val periodLabel: String,
    val amount: Long,
    val isVisibleToJemaat: Boolean,
    val sectorName: String
)

data class FinanceReportDetail(
    val id: String,
    val title: String,
    val description: String,
    val periodLabel: String,
    val amount: Long,
    val isVisibleToJemaat: Boolean,
    val sectorId: String,
    val sectorName: String
) {
    fun toSummary(): FinanceReportSummary {
        return FinanceReportSummary(
            id = id,
            title = title,
            periodLabel = periodLabel,
            amount = amount,
            isVisibleToJemaat = isVisibleToJemaat,
            sectorName = sectorName
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
