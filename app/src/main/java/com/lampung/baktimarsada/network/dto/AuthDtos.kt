package com.lampung.baktimarsada.network.dto

data class LoginRequestDto(
    val identifier: String,
    val password: String
)

data class BackendLoginRequestDto(
    val email: String,
    val password: String
)

data class SessionResponseDto(
    val authToken: String,
    val userId: String,
    val displayName: String,
    val role: String,
    val sectorId: String,
    val sectorName: String
)

data class BackendAuthResponseDto(
    val user: BackendUserDto? = null,
    val session: BackendSessionDto? = null,
    val authToken: String? = null,
    val userId: String? = null,
    val displayName: String? = null,
    val role: String? = null,
    val sectorId: String? = null,
    val sectorName: String? = null
)

data class BackendUserDto(
    val id: String? = null,
    val fullName: String? = null,
    val role: String? = null
)

data class BackendSessionDto(
    val sessionToken: String? = null
)
