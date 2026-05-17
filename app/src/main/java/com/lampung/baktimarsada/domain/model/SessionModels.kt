package com.lampung.baktimarsada.domain.model

enum class UserRole {
    JEMAAT,
    ADMIN
}

data class SectorContext(
    val sectorId: String,
    val sectorName: String
)

data class SessionState(
    val authToken: String,
    val userId: String,
    val displayName: String,
    val role: UserRole,
    val sectorContext: SectorContext
)
