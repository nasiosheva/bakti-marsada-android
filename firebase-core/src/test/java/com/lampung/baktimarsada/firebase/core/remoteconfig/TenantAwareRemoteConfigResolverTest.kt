package com.lampung.baktimarsada.firebase.core.remoteconfig

import org.junit.Assert.assertEquals
import org.junit.Test

class TenantAwareRemoteConfigResolverTest {

    private val resolver = DefaultTenantAwareRemoteConfigResolver()

    @Test
    fun `build tenant scoped key uses current tenant`() {
        assertEquals(
            "tenant.hkbp-kedaton.app.home_banner_title",
            resolver.buildTenantScopedKey("app.home_banner_title")
        )
    }

    @Test
    fun `resolve returns tenant key when key exists`() {
        val resolved = resolver.resolve("app.home_banner_title") { key ->
            key == "tenant.hkbp-kedaton.app.home_banner_title"
        }

        assertEquals("tenant.hkbp-kedaton.app.home_banner_title", resolved)
    }

    @Test
    fun `resolve falls back to global key when tenant key missing`() {
        val resolved = resolver.resolve("app.home_banner_title") { false }

        assertEquals("app.home_banner_title", resolved)
    }
}
