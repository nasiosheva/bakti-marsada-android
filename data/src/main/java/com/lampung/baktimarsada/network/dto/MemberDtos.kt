package com.lampung.baktimarsada.network.dto

data class MemberDto(
    val id: String,
    val fullName: String,
    val familyGroup: String,
    val phoneNumber: String,
    val address: String,
    val roleInSector: String,
    val sectorId: String,
    val sectorName: String,
    val tenantId: String = ""
)

// created by Mories Deo Hutapea, S.E.,S.Kom
