package com.lampung.baktimarsada.domain.model

data class ArisanGroup(
    val id: String,
    val title: String,
    val sectorId: String,
    val sectorName: String,
    val contributionAmount: Long,
    val activePeriod: ArisanPeriod?,
    val participants: List<ArisanParticipant>
)

data class ArisanPeriod(
    val id: String,
    val label: String,
    val contributionAmount: Long,
    val dueDate: String,
    val winnerMemberId: String?,
    val winnerMemberName: String?
)

data class ArisanParticipant(
    val memberId: String,
    val memberName: String,
    val paymentStatus: PaymentStatus?
) {
    val hasPaid: Boolean
        get() = paymentStatus == PaymentStatus.PAID
}

// created by Mories Deo Hutapea, S.E.,S.Kom
