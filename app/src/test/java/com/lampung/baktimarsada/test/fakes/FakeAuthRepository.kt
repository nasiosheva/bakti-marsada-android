package com.lampung.baktimarsada.test.fakes

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(
    initialSession: SessionState? = null
) : AuthRepository {
    private val sessionFlow = MutableStateFlow(initialSession)
    var loginResult: AppResult<SessionState> = initialSession?.let { AppResult.Success(it) }
        ?: AppResult.Error("No session")
    var syncedToken: String? = null

    override fun observeSession(): Flow<SessionState?> = sessionFlow

    override suspend fun bootstrapSession(): SessionState? = sessionFlow.value

    override suspend fun getCurrentSession(): SessionState? = sessionFlow.value

    override suspend fun login(identifier: String, password: String): AppResult<SessionState> {
        return loginResult.also { result ->
            if (result is AppResult.Success) {
                sessionFlow.value = result.data
            }
        }
    }

    override suspend fun logout() {
        sessionFlow.value = null
    }

    override suspend fun syncFcmToken(token: String): AppResult<Unit> {
        syncedToken = token
        return AppResult.Success(Unit)
    }

    fun updateSession(session: SessionState?) {
        sessionFlow.value = session
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
