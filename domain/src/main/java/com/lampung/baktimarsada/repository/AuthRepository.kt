package com.lampung.baktimarsada.repository

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.SessionState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeSession(): Flow<SessionState?>
    suspend fun bootstrapSession(): SessionState?
    suspend fun getCurrentSession(): SessionState?
    suspend fun login(identifier: String, password: String): AppResult<SessionState>
    suspend fun loginWithGoogle(idToken: String): AppResult<SessionState>
    suspend fun registerNewTenant(
        churchName: String,
        denomination: String,
        adminFullName: String,
        adminEmail: String,
        adminPassword: String,
        terminologyPreset: String = ""
    ): AppResult<SessionState>
    suspend fun logout()
    suspend fun syncFcmToken(token: String): AppResult<Unit>
}

// created by Mories Deo Hutapea, S.E.,S.Kom
