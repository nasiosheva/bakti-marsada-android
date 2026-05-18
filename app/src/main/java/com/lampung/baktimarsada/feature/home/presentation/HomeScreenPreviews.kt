package com.lampung.baktimarsada.feature.home.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.TenantContext
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

private val previewSession = SessionState(
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

private val previewSessionJemaat = previewSession.copy(role = UserRole.JEMAAT)

@Preview(name = "Home Jemaat")
@Composable
private fun JemaatHomeScreenPreview() {
    BaktiMarsadaTheme {
        JemaatHomeScreen(
            session = previewSessionJemaat,
            onLogout = {},
            eventsContent = { _ -> Text("Events placeholder") },
            membersContent = { Text("Members placeholder") },
            financeContent = { Text("Finance placeholder") },
            paymentsContent = { Text("Payments placeholder") },
            arisanContent = { _ -> Text("Arisan placeholder") },
            onOpenProfile = {}
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
