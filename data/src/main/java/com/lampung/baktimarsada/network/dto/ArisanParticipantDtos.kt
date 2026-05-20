package com.lampung.baktimarsada.network.dto

data class ArisanParticipantDto(
    val memberId: String,
    val memberName: String,
    val tenantId: String = ""
)

data class ReplaceArisanParticipantsRequestDto(
    val sectorId: String,
    val memberIds: List<String>,
    val tenantId: String = ""
)

data class FillArisanParticipantsRequestDto(
    val sectorId: String,
    val tenantId: String = ""
)

// created by Mories Deo Hutapea, S.E.,S.Kom
