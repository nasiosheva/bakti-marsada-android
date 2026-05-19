package com.lampung.baktimarsada.network.dto

data class ArisanParticipantDto(
    val memberId: String,
    val memberName: String
)

data class ReplaceArisanParticipantsRequestDto(
    val sectorId: String,
    val memberIds: List<String>
)

data class FillArisanParticipantsRequestDto(
    val sectorId: String
)

// created by Mories Deo Hutapea, S.E.,S.Kom
