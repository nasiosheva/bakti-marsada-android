package com.lampung.baktimarsada.domain.model

enum class PaymentStatus {
    UNPAID,
    PAID,
    OVERDUE
}

data class PaymentObligationSummary(
    val id: String,
    val title: String,
    val memberName: String,
    val amount: Long,
    val dueDate: String,
    val status: PaymentStatus,
    val sectorName: String
)

data class PaymentObligationDetail(
    val id: String,
    val memberId: String,
    val memberName: String,
    val title: String,
    val description: String,
    val amount: Long,
    val dueDate: String,
    val status: PaymentStatus,
    val sectorId: String,
    val sectorName: String
) {
    fun toSummary(): PaymentObligationSummary {
        return PaymentObligationSummary(
            id = id,
            title = title,
            memberName = memberName,
            amount = amount,
            dueDate = dueDate,
            status = status,
            sectorName = sectorName
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
