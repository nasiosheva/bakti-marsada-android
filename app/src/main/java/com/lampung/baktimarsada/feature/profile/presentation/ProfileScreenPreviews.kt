package com.lampung.baktimarsada.feature.profile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.TenantContext
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

private val profileSession = SessionState(
    authToken = "preview",
    userId = "u-1",
    displayName = "Bakti Marsada",
    role = UserRole.ADMIN,
    tenantContext = TenantContext(
        tenantId = "t-1",
        tenantName = "Gereja",
        subTenantId = "s-1",
        subTenantName = "Jakarta"
    ),
    sectorContext = SectorContext(sectorId = "sec-1", sectorName = "Lingkungan 1")
)

@Preview(name = "Profile")
@Composable
private fun ProfileScreenPreview() {
    BaktiMarsadaTheme {
        ProfileScreen(session = profileSession, onLogout = {})
    }
}

@Preview(name = "Profile with back")
@Composable
private fun ProfileScreenWithBackPreview() {
    BaktiMarsadaTheme {
        ProfileScreen(session = profileSession, onLogout = {}, onBack = {})
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
