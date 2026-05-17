package com.lampung.baktimarsada.network.dto

data class PaymentObligationDto(
    val id: String,
    val memberId: String,
    val memberName: String,
    val title: String,
    val description: String,
    val amount: Long,
    val dueDate: String,
    val status: String,
    val sectorId: String,
    val sectorName: String
)

// created by Mories Deo Hutapea, S.E.,S.Kom
