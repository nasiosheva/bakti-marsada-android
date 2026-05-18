package com.lampung.baktimarsada.data.repository

import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.data.mapper.toDomain
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.TenantContext
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.repository.AuthRepository
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.security.SecureStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val secureStorage: SecureStorage,
    private val remoteDataSource: AppRemoteDataSource
) : AuthRepository {

    private val sessionStateFlow = MutableStateFlow<SessionState?>(readStoredSession())

    override fun observeSession(): Flow<SessionState?> = sessionStateFlow.asStateFlow()

    override suspend fun bootstrapSession(): SessionState? {
        val restored = readStoredSession()
        sessionStateFlow.value = restored
        return restored
    }

    override suspend fun getCurrentSession(): SessionState? = sessionStateFlow.value

    override suspend fun login(identifier: String, password: String): AppResult<SessionState> {
        return runCatching {
            remoteDataSource.login(
                LoginRequestDto(
                    identifier = identifier,
                    password = password
                )
            ).toDomain()
        }.fold(
            onSuccess = { session ->
                persistSession(session)
                sessionStateFlow.value = session
                secureStorage.getString(AppConstants.KEY_FCM_TOKEN)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { token ->
                        runCatching { remoteDataSource.syncFcmToken(token) }
                    }
                AppResult.Success(session)
            },
            onFailure = { throwable ->
                AppResult.Error(
                    message = throwable.message ?: "Login failed",
                    cause = throwable
                )
            }
        )
    }

    override suspend fun logout() {
        sessionStateFlow.value?.authToken?.let { token ->
            runCatching { remoteDataSource.logout(token) }
        }
        clearStoredSession()
        sessionStateFlow.value = null
    }

    override suspend fun syncFcmToken(token: String): AppResult<Unit> {
        secureStorage.putString(AppConstants.KEY_FCM_TOKEN, token)
        return runCatching {
            remoteDataSource.syncFcmToken(token)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable ->
                AppResult.Error(
                    message = throwable.message ?: "FCM sync failed",
                    cause = throwable
                )
            }
        )
    }

    private fun persistSession(session: SessionState) {
        secureStorage.putString(AppConstants.KEY_AUTH_TOKEN, session.authToken)
        secureStorage.putString(AppConstants.KEY_USER_ID, session.userId)
        secureStorage.putString(AppConstants.KEY_DISPLAY_NAME, session.displayName)
        secureStorage.putString(AppConstants.KEY_USER_ROLE, session.role.name)
        secureStorage.putString(AppConstants.KEY_TENANT_ID, session.tenantContext.tenantId)
        secureStorage.putString(AppConstants.KEY_TENANT_NAME, session.tenantContext.tenantName)
        secureStorage.putString(AppConstants.KEY_SUB_TENANT_ID, session.tenantContext.subTenantId)
        secureStorage.putString(AppConstants.KEY_SUB_TENANT_NAME, session.tenantContext.subTenantName)
        secureStorage.putString(AppConstants.KEY_SECTOR_ID, session.sectorContext.sectorId)
        secureStorage.putString(AppConstants.KEY_SECTOR_NAME, session.sectorContext.sectorName)
    }

    private fun clearStoredSession() {
        secureStorage.remove(AppConstants.KEY_AUTH_TOKEN)
        secureStorage.remove(AppConstants.KEY_USER_ID)
        secureStorage.remove(AppConstants.KEY_DISPLAY_NAME)
        secureStorage.remove(AppConstants.KEY_USER_ROLE)
        secureStorage.remove(AppConstants.KEY_TENANT_ID)
        secureStorage.remove(AppConstants.KEY_TENANT_NAME)
        secureStorage.remove(AppConstants.KEY_SUB_TENANT_ID)
        secureStorage.remove(AppConstants.KEY_SUB_TENANT_NAME)
        secureStorage.remove(AppConstants.KEY_SECTOR_ID)
        secureStorage.remove(AppConstants.KEY_SECTOR_NAME)
    }

    private fun readStoredSession(): SessionState? {
        val token = secureStorage.getString(AppConstants.KEY_AUTH_TOKEN).orEmpty()
        val userId = secureStorage.getString(AppConstants.KEY_USER_ID).orEmpty()
        val displayName = secureStorage.getString(AppConstants.KEY_DISPLAY_NAME).orEmpty()
        val role = secureStorage.getString(AppConstants.KEY_USER_ROLE).orEmpty()
        val tenantId = secureStorage.getString(AppConstants.KEY_TENANT_ID).orEmpty()
        val tenantName = secureStorage.getString(AppConstants.KEY_TENANT_NAME).orEmpty()
        val subTenantId = secureStorage.getString(AppConstants.KEY_SUB_TENANT_ID).orEmpty()
        val subTenantName = secureStorage.getString(AppConstants.KEY_SUB_TENANT_NAME).orEmpty()
        val sectorId = secureStorage.getString(AppConstants.KEY_SECTOR_ID).orEmpty()
        val sectorName = secureStorage.getString(AppConstants.KEY_SECTOR_NAME).orEmpty()
        if (
            token.isBlank() ||
            userId.isBlank() ||
            role.isBlank() ||
            tenantId.isBlank() ||
            tenantName.isBlank() ||
            subTenantId.isBlank() ||
            subTenantName.isBlank() ||
            sectorId.isBlank() ||
            sectorName.isBlank()
        ) {
            return null
        }
        return SessionState(
            authToken = token,
            userId = userId,
            displayName = displayName.ifBlank { userId },
            role = UserRole.valueOf(role),
            tenantContext = TenantContext(
                tenantId = tenantId,
                tenantName = tenantName,
                subTenantId = subTenantId,
                subTenantName = subTenantName
            ),
            sectorContext = SectorContext(
                sectorId = sectorId,
                sectorName = sectorName
            )
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
