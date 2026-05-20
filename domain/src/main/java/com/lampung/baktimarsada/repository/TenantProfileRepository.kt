package com.lampung.baktimarsada.repository

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.TenantProfileSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Holds the active tenant profile snapshot fetched from the server. Used by UI to render
 * branding (name, logo, theme) and to provide per-tenant terminology.
 */
interface TenantProfileRepository {
    /** Observe the current cached snapshot. Null means "not yet fetched". */
    fun observeProfile(): Flow<TenantProfileSnapshot?>

    /** Synchronous accessor for non-Compose callers. */
    fun getCachedProfile(): TenantProfileSnapshot?

    /** Fetch from server and update cache. Should be called after every successful login. */
    suspend fun refresh(): AppResult<TenantProfileSnapshot>
}

// created by Mories Deo Hutapea, S.E.,S.Kom
