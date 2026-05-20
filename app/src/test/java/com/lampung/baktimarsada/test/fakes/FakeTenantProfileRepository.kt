package com.lampung.baktimarsada.test.fakes

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.TenantProfileSnapshot
import com.lampung.baktimarsada.domain.model.TenantTerminology
import com.lampung.baktimarsada.repository.TenantProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTenantProfileRepository(
    initialProfile: TenantProfileSnapshot? = null
) : TenantProfileRepository {

    private val cache = MutableStateFlow(initialProfile)

    override fun observeProfile(): Flow<TenantProfileSnapshot?> = cache.asStateFlow()

    override fun getCachedProfile(): TenantProfileSnapshot? = cache.value

    override suspend fun refresh(): AppResult<TenantProfileSnapshot> {
        val current = cache.value ?: defaultSnapshot()
        cache.value = current
        return AppResult.Success(current)
    }

    fun update(profile: TenantProfileSnapshot?) {
        cache.value = profile
    }

    private fun defaultSnapshot() = TenantProfileSnapshot(
        tenantId = "fake-tenant",
        tenantName = "Fake Tenant",
        subTenantId = "fake-sub",
        subTenantName = "Fake Sub",
        denomination = "HKBP",
        appDisplayName = "Bakti Marsada",
        logoUrl = "",
        themeColor = "",
        terminology = TenantTerminology()
    )
}

// created by Mories Deo Hutapea, S.E.,S.Kom
