package com.lampung.baktimarsada.core.tenant

data class TenantProfile(
    val key: String,
    val appDisplayName: String,
    val tenantId: String,
    val tenantName: String,
    val subTenantId: String,
    val subTenantName: String,
    val defaultSectorId: String,
    val defaultSectorName: String,
    val sampleAdminIdentifier: String,
    val sampleAdminPassword: String,
    val sampleJemaatIdentifier: String,
    val sampleJemaatPassword: String
)
