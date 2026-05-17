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
    val sectorName: String
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
