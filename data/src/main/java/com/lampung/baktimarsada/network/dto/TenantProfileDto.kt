package com.lampung.baktimarsada.network.dto

/**
 * Server-supplied tenant profile. Returned by `GET /tenants/me/profile` after login.
 *
 * Most fields mirror the build-time [com.lampung.baktimarsada.core.tenant.TenantProfile] so the
 * client can switch entirely from a hard-coded registry to a backend-driven configuration.
 * The optional [terminology] map enables per-tenant labels (e.g. "Sektor" vs "Lingkungan").
 */
data class TenantProfileDto(
    val tenantId: String,
    val tenantName: String,
    val subTenantId: String = "",
    val subTenantName: String = "",
    val denomination: String = "",
    val appDisplayName: String = "",
    val logoUrl: String = "",
    val themeColor: String = "",
    val terminology: TenantTerminologyDto = TenantTerminologyDto()
)

data class TenantTerminologyDto(
    val sectorLabel: String = "",
    val sectorPluralLabel: String = "",
    val gatheringLabel: String = "",
    val memberLabel: String = "",
    val financeLabel: String = "",
    val paymentLabel: String = "",
    val arisanLabel: String = ""
)

// created by Mories Deo Hutapea, S.E.,S.Kom
