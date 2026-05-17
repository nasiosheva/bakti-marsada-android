package com.lampung.baktimarsada.feature.events.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.EventProgramItem
import com.lampung.baktimarsada.domain.model.ProgramItemType
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.TenantContext
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

private val eventDetail = EventDetail(
    id = "e-1",
    title = "Ibadah Jumat",
    description = "Renungan singkat dan persembahan",
    scheduledAt = "Jumat, 1 Mei 2026",
    location = "Gedung Serbaguna",
    sectorId = "sec-1",
    sectorName = "Lingkungan 1",
    programItems = listOf(
        EventProgramItem(
            id = "p1",
            eventId = "e-1",
            orderIndex = 1,
            title = "Pembukaan",
            content = "Pujian pembuka",
            leader = "Wakil Pastor",
            type = ProgramItemType.OPENING
        ),
        EventProgramItem(
            id = "p2",
            eventId = "e-1",
            orderIndex = 2,
            title = "Ayat",
            content = "Ayat Firman",
            leader = "Pelayan",
            type = ProgramItemType.SCRIPTURE,
            scriptureReference = "John 3:16"
        )
    )
)

private val eventDetailSession = SessionState(
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
    sectorContext = SectorContext(
        sectorId = "sec-1",
        sectorName = "Lingkungan 1"
    )
)

@Preview(name = "Event Detail - admin")
@Composable
private fun EventDetailContentAdminPreview() {
    BaktiMarsadaTheme {
        EventDetailContent(
            item = eventDetail,
            isAdmin = true,
            session = eventDetailSession
        )
    }
}

@Preview(name = "Event Detail - jemaat")
@Composable
private fun EventDetailContentJemaatPreview() {
    BaktiMarsadaTheme {
        EventDetailContent(
            item = eventDetail,
            isAdmin = false,
            session = eventDetailSession.copy(role = UserRole.JEMAAT)
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
