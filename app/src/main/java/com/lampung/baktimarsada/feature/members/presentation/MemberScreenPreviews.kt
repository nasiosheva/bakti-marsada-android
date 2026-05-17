package com.lampung.baktimarsada.feature.members.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

private val previewMembers = listOf(
    MemberDetail(
        id = "m-1",
        fullName = "Andi Pratama",
        familyGroup = "Keluarga A",
        phoneNumber = "081234567890",
        address = "Jl. Mawar No. 10",
        roleInSector = "Ketua Lingkungan",
        sectorId = "sec-1",
        sectorName = "Lingkungan 1"
    )
)

@Preview(name = "Members - admin")
@Composable
private fun MemberContentAdminPreview() {
    BaktiMarsadaTheme {
        MemberContent(
            isAdmin = true,
            state = MemberUiState(items = previewMembers, isLoading = false),
            onRefresh = {},
            onDelete = {},
            onShowCreate = {},
            onShowDetail = {},
            onShowEdit = {}
        )
    }
}

@Preview(name = "Members - jemaat")
@Composable
private fun MemberContentJemaatPreview() {
    BaktiMarsadaTheme {
        MemberContent(
            isAdmin = false,
            state = MemberUiState(items = previewMembers, isLoading = false),
            onRefresh = {},
            onDelete = {},
            onShowCreate = {},
            onShowDetail = {},
            onShowEdit = {}
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
