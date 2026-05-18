package com.lampung.baktimarsada.test.fakes

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.TenantContext
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(
    initialSession: SessionState? = null,
    private val fixedRole: UserRole? = null
) : AuthRepository {
    private val sessionFlow = MutableStateFlow(initialSession)
    var loginResult: AppResult<SessionState> = initialSession?.let { AppResult.Success(it) }
        ?: AppResult.Error("No session")
    var syncedToken: String? = null
    var lastIdentifier: String? = null
    var lastPassword: String? = null
    var lastGoogleToken: String? = null

    override fun observeSession(): Flow<SessionState?> = sessionFlow

    override suspend fun bootstrapSession(): SessionState? = sessionFlow.value

    override suspend fun getCurrentSession(): SessionState? = sessionFlow.value

    override suspend fun login(identifier: String, password: String): AppResult<SessionState> {
        lastIdentifier = identifier
        lastPassword = password
        if (fixedRole != null) {
            val session = buildSession(identifier = identifier, role = fixedRole)
            sessionFlow.value = session
            return AppResult.Success(session)
        }
        return loginResult.also { result ->
            if (result is AppResult.Success) {
                sessionFlow.value = result.data
            }
        }
    }

    override suspend fun loginWithGoogle(idToken: String): AppResult<SessionState> {
        lastGoogleToken = idToken
        if (fixedRole != null) {
            val session = buildSession(identifier = "google", role = fixedRole)
            sessionFlow.value = session
            return AppResult.Success(session)
        }
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

    private fun buildSession(identifier: String, role: UserRole): SessionState {
        return SessionState(
            authToken = "token-$identifier",
            userId = "user-$identifier",
            displayName = if (identifier == "google") "Google User" else "Demo User",
            role = role,
            tenantContext = TenantContext(
                tenantId = "hkbp",
                tenantName = "HKBP",
                subTenantId = "hkbp-kedaton",
                subTenantName = "HKBP Kedaton"
            ),
            sectorContext = SectorContext(
                sectorId = "wijk-1",
                sectorName = "Wijk Simulasi"
            )
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
