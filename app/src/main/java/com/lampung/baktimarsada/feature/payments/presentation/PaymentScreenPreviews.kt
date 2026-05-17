package com.lampung.baktimarsada.feature.payments.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.domain.model.PaymentStatus
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

private val paymentMembers = listOf(
    MemberDetail(
        id = "m-1",
        fullName = "Andi Pratama",
        familyGroup = "Keluarga A",
        phoneNumber = "081234567890",
        address = "Jl. Mawar No. 10",
        roleInSector = "Warga",
        sectorId = "sec-1",
        sectorName = "Lingkungan 1"
    )
)

private val paymentItems = listOf(
    PaymentObligationDetail(
        id = "p-1",
        memberId = "m-1",
        memberName = "Andi Pratama",
        title = "Iuran Bulanan",
        description = "Iuran kas jemaat",
        amount = 100000,
        dueDate = "2026-05-31",
        status = PaymentStatus.UNPAID,
        sectorId = "sec-1",
        sectorName = "Lingkungan 1"
    )
)

@Preview(name = "Payments - admin")
@Composable
private fun PaymentContentAdminPreview() {
    BaktiMarsadaTheme {
        PaymentContent(
            isAdmin = true,
            state = PaymentUiState(
                items = paymentItems,
                members = paymentMembers,
                isLoading = false
            ),
            onRefresh = {},
            onDelete = {},
            onShowCreate = {},
            onShowDetail = {},
            onShowEdit = {}
        )
    }
}

@Preview(name = "Payments - jemaat")
@Composable
private fun PaymentContentJemaatPreview() {
    BaktiMarsadaTheme {
        PaymentContent(
            isAdmin = false,
            state = PaymentUiState(
                items = paymentItems,
                members = paymentMembers,
                isLoading = false
            ),
            onRefresh = {},
            onDelete = {},
            onShowCreate = {},
            onShowDetail = {},
            onShowEdit = {}
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
