package com.lampung.baktimarsada.domain.model

data class EventSummary(
    val id: String,
    val title: String,
    val scheduledAt: String,
    val location: String,
    val sectorName: String
)

data class EventDetail(
    val id: String,
    val title: String,
    val description: String,
    val scheduledAt: String,
    val location: String,
    val sectorId: String,
    val sectorName: String,
    val programItems: List<EventProgramItem> = emptyList()
) {
    fun toSummary(): EventSummary {
        return EventSummary(
            id = id,
            title = title,
            scheduledAt = scheduledAt,
            location = location,
            sectorName = sectorName
        )
    }
}

enum class ProgramItemType {
    OPENING,
    SONG,
    PRAYER,
    SCRIPTURE,
    SERMON,
    OFFERING,
    ANNOUNCEMENT,
    CLOSING,
    CUSTOM
}

data class EventProgramItem(
    val id: String,
    val eventId: String,
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
