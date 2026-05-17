package com.lampung.baktimarsada.feature.events.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.ProgramItemType
import com.lampung.baktimarsada.domain.model.WorshipTemplate
import com.lampung.baktimarsada.domain.model.WorshipTemplateItem
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

private val eventDetail = EventDetail(
    id = "e-1",
    title = "Ibadah Minggu",
    description = "Pembukaan, firman Tuhan, dan persembahan",
    scheduledAt = "Sabtu, 17 Mei 2026",
    location = "Aula Utama",
    sectorId = "sec-1",
    sectorName = "Lingkungan 1"
)

private val eventTemplate = WorshipTemplate(
    id = "tmpl-1",
    tenantId = "tenant-1",
    sectorId = "sec-1",
    title = "Template Ibadah",
    description = "Template harian",
    items = listOf(
        WorshipTemplateItem(
            id = "itm-1",
            templateId = "tmpl-1",
            orderIndex = 1,
            title = "Pembukaan",
            content = "Doa pembuka",
            leader = "Pastor",
            type = ProgramItemType.OPENING
        )
    )
)

@Preview(name = "Events - admin")
@Composable
private fun EventContentAdminPreview() {
    BaktiMarsadaTheme {
        EventContent(
            isAdmin = true,
            state = EventUiState(
                items = listOf(eventDetail),
                templates = listOf(eventTemplate),
                isLoading = false
            ),
            onRefresh = {},
            onDelete = {},
            onShowCreate = {},
            onShowTemplates = {},
            onShowDetail = {},
            onShowEdit = {}
        )
    }
}

@Preview(name = "Events - jemaat")
@Composable
private fun EventContentJemaatPreview() {
    BaktiMarsadaTheme {
        EventContent(
            isAdmin = false,
            state = EventUiState(
                items = listOf(eventDetail),
                templates = listOf(eventTemplate),
                isLoading = false
            ),
            onRefresh = {},
            onDelete = {},
            onShowCreate = {},
            onShowTemplates = {},
            onShowDetail = {},
            onShowEdit = {}
        )
    }
}

@Preview(name = "Events - loading")
@Composable
private fun EventContentLoadingPreview() {
    BaktiMarsadaTheme {
        EventContent(
            isAdmin = true,
            state = EventUiState(isLoading = true),
            onRefresh = {},
            onDelete = {},
            onShowCreate = {},
            onShowTemplates = {},
            onShowDetail = {},
            onShowEdit = {}
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
