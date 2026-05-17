package com.lampung.baktimarsada.network.dto

data class ApiResponseDto<T>(
    val ok: Boolean,
    val code: String? = null,
    val message: String? = null,
    val data: T? = null
)
