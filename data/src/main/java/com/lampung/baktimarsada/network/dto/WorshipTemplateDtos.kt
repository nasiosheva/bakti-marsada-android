package com.lampung.baktimarsada.network.dto

data class WorshipTemplateDto(
    val id: String,
    val tenantId: String,
    val sectorId: String?,
    val title: String,
    val description: String,
    val items: List<WorshipTemplateItemDto> = emptyList()
)

data class WorshipTemplateItemDto(
    val id: String,
    val templateId: String,
    val orderIndex: Int,
    val title: String,
    val content: String,
    val leader: String,
    val type: String,
    val scriptureReference: String = "",
    val scriptureText: String = "",
    val note: String = ""
)

// created by Mories Deo Hutapea, S.E.,S.Kom
