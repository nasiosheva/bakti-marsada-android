package com.lampung.baktimarsada.core.tenant

object TenantRegistry {
    const val DEFAULT_TENANT_KEY = "hkbp-kedaton"

    private val tenants = mapOf(
        DEFAULT_TENANT_KEY to TenantProfile(
            key = DEFAULT_TENANT_KEY,
            appDisplayName = "Bakti Marsada",
            tenantId = "hkbp",
            tenantName = "HKBP",
            subTenantId = "hkbp-kedaton",
            subTenantName = "HKBP Kedaton",
            defaultSectorId = "sector-1",
            defaultSectorName = "Sektor 1 HKBP",
            sampleAdminIdentifier = "admin@baktimarsada.id",
            sampleAdminPassword = "admin123",
            sampleJemaatIdentifier = "jemaat@baktimarsada.id",
            sampleJemaatPassword = "jemaat123"
        ),
        "hkbp-bandarjaya" to TenantProfile(
            key = "hkbp-bandarjaya",
            appDisplayName = "Bakti HKBP",
            tenantId = "hkbp",
            tenantName = "HKBP",
            subTenantId = "hkbp-bandarjaya",
            subTenantName = "HKBP Bandar Jaya",
            defaultSectorId = "sector-1",
            defaultSectorName = "Sektor 1 HKBP",
            sampleAdminIdentifier = "admin@hkbpbandarjaya.id",
            sampleAdminPassword = "admin123",
            sampleJemaatIdentifier = "jemaat@hkbpbandarjaya.id",
            sampleJemaatPassword = "jemaat123"
        )
    )

    fun get(key: String?): TenantProfile {
        return tenants[key] ?: tenants.getValue(DEFAULT_TENANT_KEY)
    }
}
