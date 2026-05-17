package com.lampung.baktimarsada.domain.model

data class MemberSummary(
    val id: String,
    val fullName: String,
    val familyGroup: String,
    val roleInSector: String,
    val sectorName: String
)

data class MemberDetail(
    val id: String,
    val fullName: String,
    val familyGroup: String,
    val phoneNumber: String,
    val address: String,
    val roleInSector: String,
    val sectorId: String,
    val sectorName: String
) {
    fun toSummary(): MemberSummary {
        return MemberSummary(
            id = id,
            fullName = fullName,
            familyGroup = familyGroup,
            roleInSector = roleInSector,
            sectorName = sectorName
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
