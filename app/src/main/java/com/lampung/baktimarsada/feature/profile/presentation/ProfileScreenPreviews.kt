package com.lampung.baktimarsada.feature.profile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.TenantContext
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

private val adminProfileSession = SessionState(
    authToken = "preview",
    userId = "u-1",
    displayName = "Mories Deo Hutapea",
    role = UserRole.ADMIN,
    tenantContext = TenantContext(
        tenantId = "t-1",
        tenantName = "HKBP",
        subTenantId = "s-1",
        subTenantName = "HKBP Kedaton"
    ),
    sectorContext = SectorContext(sectorId = "sec-1", sectorName = "Sektor 1 Kedaton")
)

private val jemaatProfileSession = SessionState(
    authToken = "preview",
    userId = "u-2",
    displayName = "Bakti Marsada",
    role = UserRole.JEMAAT,
    tenantContext = TenantContext(
        tenantId = "t-1",
        tenantName = "HKBP",
        subTenantId = "s-1",
        subTenantName = "HKBP Kedaton"
    ),
    sectorContext = SectorContext(sectorId = "sec-1", sectorName = "Sektor 1 Kedaton")
)

@Preview(name = "Profile - Admin", showBackground = true)
@Composable
private fun ProfileScreenAdminPreview() {
    BaktiMarsadaTheme {
        ProfileScreen(
            session = adminProfileSession,
            onLogout = {},
            onTestSendNotification = {}
        )
    }
}

@Preview(name = "Profile - Jemaat", showBackground = true)
@Composable
private fun ProfileScreenJemaatPreview() {
    BaktiMarsadaTheme {
        ProfileScreen(
            session = jemaatProfileSession,
            onLogout = {},
            onTestSendNotification = {}
        )
    }
}

@Preview(name = "Profile - With Back", showBackground = true)
@Composable
private fun ProfileScreenWithBackPreview() {
    BaktiMarsadaTheme {
        ProfileScreen(
            session = adminProfileSession,
            onLogout = {},
            onTestSendNotification = {},
            onBack = {}
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
