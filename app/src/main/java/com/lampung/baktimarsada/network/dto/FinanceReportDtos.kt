package com.lampung.baktimarsada.network.dto

data class FinanceReportDto(
    val id: String,
    val title: String,
    val description: String,
    val periodLabel: String,
    val amount: Long,
    val isVisibleToJemaat: Boolean,
    val sectorId: String,
    val sectorName: String
)
