package com.lampung.baktimarsada.domain.model

/**
 * Server-driven tenant profile snapshot. Fetched after login by [TenantProfileRepository] and
 * consumed by UI to render branding + terminology that varies per church.
 */
data class TenantProfileSnapshot(
    val tenantId: String,
    val tenantName: String,
    val subTenantId: String,
    val subTenantName: String,
    val denomination: String,
    val appDisplayName: String,
    val logoUrl: String,
    val themeColor: String,
    val terminology: TenantTerminology
)

/**
 * Per-tenant labels. Empty strings mean "use the default" (string resource fallback).
 */
data class TenantTerminology(
    val sectorLabel: String = "",
    val sectorPluralLabel: String = "",
    val gatheringLabel: String = "",
    val memberLabel: String = "",
    val financeLabel: String = "",
    val paymentLabel: String = "",
    val arisanLabel: String = ""
)

// created by Mories Deo Hutapea, S.E.,S.Kom
