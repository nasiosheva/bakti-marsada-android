package com.lampung.baktimarsada.data.repository

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.domain.model.TenantProfileSnapshot
import com.lampung.baktimarsada.domain.model.TenantTerminology
import com.lampung.baktimarsada.network.dto.TenantProfileDto
import com.lampung.baktimarsada.network.dto.TenantTerminologyDto
import com.lampung.baktimarsada.repository.AuthRepository
import com.lampung.baktimarsada.repository.TenantProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TenantProfileRepositoryImpl @Inject constructor(
    private val remoteDataSource: AppRemoteDataSource,
    private val authRepository: AuthRepository
) : TenantProfileRepository {

    private val cache = MutableStateFlow<TenantProfileSnapshot?>(null)

    override fun observeProfile(): Flow<TenantProfileSnapshot?> = cache.asStateFlow()

    override fun getCachedProfile(): TenantProfileSnapshot? = cache.value

    override suspend fun refresh(): AppResult<TenantProfileSnapshot> {
        return runCatching {
            val tenantId = authRepository.getCurrentSession()?.tenantContext?.tenantId.orEmpty()
            val dto = remoteDataSource.fetchTenantProfile(tenantId)
            dto.toSnapshot().also { cache.value = it }
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { throwable ->
                AppResult.Error(throwable.message ?: "Failed to load tenant profile", throwable)
            }
        )
    }
}

private fun TenantProfileDto.toSnapshot(): TenantProfileSnapshot = TenantProfileSnapshot(
    tenantId = tenantId,
    tenantName = tenantName,
    subTenantId = subTenantId,
    subTenantName = subTenantName,
    denomination = denomination,
    appDisplayName = appDisplayName,
    logoUrl = logoUrl,
    themeColor = themeColor,
    terminology = terminology.toDomain()
)

private fun TenantTerminologyDto.toDomain(): TenantTerminology = TenantTerminology(
    sectorLabel = sectorLabel,
    sectorPluralLabel = sectorPluralLabel,
    gatheringLabel = gatheringLabel,
    memberLabel = memberLabel,
    financeLabel = financeLabel,
    paymentLabel = paymentLabel,
    arisanLabel = arisanLabel
)

// created by Mories Deo Hutapea, S.E.,S.Kom
