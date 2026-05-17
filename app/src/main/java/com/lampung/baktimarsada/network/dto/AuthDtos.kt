package com.lampung.baktimarsada.network.dto

data class LoginRequestDto(
    val identifier: String,
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
