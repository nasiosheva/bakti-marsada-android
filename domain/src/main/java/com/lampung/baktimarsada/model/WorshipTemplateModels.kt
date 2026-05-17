package com.lampung.baktimarsada.domain.model

data class WorshipTemplate(
    val id: String,
    val tenantId: String,
    val sectorId: String?,
    val title: String,
    val description: String,
    val items: List<WorshipTemplateItem> = emptyList()
)

data class WorshipTemplateItem(
    val id: String,
    val templateId: String,
    val orderIndex: Int,
    val title: String,
    val content: String,
    val leader: String,
    val type: ProgramItemType,
    val scriptureReference: String = "",
    val scriptureText: String = "",
    val note: String = ""
)

// created by Mories Deo Hutapea, S.E.,S.Kom
