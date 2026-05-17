package com.lampung.baktimarsada.network.dto

data class EventDto(
    val id: String,
    val title: String,
    val description: String,
    val scheduledAt: String,
    val location: String,
    val sectorId: String,
    val sectorName: String
)
