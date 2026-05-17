package com.lampung.baktimarsada.core.tenant

object TenantRuntime {
    @Volatile
    private var currentProfile: TenantProfile = TenantRegistry.get(TenantRegistry.DEFAULT_TENANT_KEY)

    val current: TenantProfile
        get() = currentProfile

    fun initialize(tenantKey: String?) {
        currentProfile = TenantRegistry.get(tenantKey)
    }
}
