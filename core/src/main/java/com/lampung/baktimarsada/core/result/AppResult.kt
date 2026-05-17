package com.lampung.baktimarsada.core.result

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val message: String, val cause: Throwable? = null) : AppResult<Nothing>
}

// created by Mories Deo Hutapea, S.E.,S.Kom
