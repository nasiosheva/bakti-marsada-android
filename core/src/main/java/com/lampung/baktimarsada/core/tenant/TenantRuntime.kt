package com.lampung.baktimarsada.core.tenant

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holder for the active tenant profile.
 *
 * The profile starts at the build-time default ([TenantRegistry.DEFAULT_TENANT_KEY]) so that
 * non-suspending callers (data source seed, fallback login paths, OkHttp interceptor) have a
 * reasonable value before login. After login succeeds, [AuthRepository] should call [setActive]
 * with a profile derived from the [com.lampung.baktimarsada.domain.model.SessionState] so that
 * subsequent network requests carry the correct tenant identity.
 *
 * Reactive observers can collect [profileFlow] to react to tenant switching (e.g. terminology
 * provider).
 */
object TenantRuntime {
    private val _profileFlow: MutableStateFlow<TenantProfile> = MutableStateFlow(
        TenantRegistry.get(TenantRegistry.DEFAULT_TENANT_KEY)
    )

    /** Reactive stream of the currently active tenant profile. */
    val profileFlow: StateFlow<TenantProfile> = _profileFlow.asStateFlow()

    /** Snapshot accessor preserved for legacy callers (interceptors, seed data, fallbacks). */
    val current: TenantProfile
        get() = _profileFlow.value

    /** Initialise from a build-time tenant key. Used at app startup before any login. */
    fun initialize(tenantKey: String?) {
        _profileFlow.value = TenantRegistry.get(tenantKey)
    }

    /**
     * Replace the active profile with one derived from the user session (post-login). Falls back
     * to the previous profile when [profile] is null or has a blank [TenantProfile.tenantId] so
     * we never end up with an empty identity at runtime.
     */
    fun setActive(profile: TenantProfile?) {
        if (profile == null || profile.tenantId.isBlank()) return
        _profileFlow.value = profile
    }

    /** Reset to the build-time default. Useful on logout. */
    fun resetToDefault() {
        _profileFlow.value = TenantRegistry.get(TenantRegistry.DEFAULT_TENANT_KEY)
    }
}
