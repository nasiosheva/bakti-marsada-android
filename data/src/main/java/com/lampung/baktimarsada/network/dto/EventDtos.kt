package com.lampung.baktimarsada.network.dto

data class EventDto(
    val id: String,
    val title: String,
    val description: String,
    val scheduledAt: String,
    val location: String,
    val sectorId: String,
    val sectorName: String,
    val tenantId: String = "",
    val programItems: List<EventProgramItemDto> = emptyList()
)

data class EventProgramItemDto(
    val id: String,
    val eventId: String,
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
