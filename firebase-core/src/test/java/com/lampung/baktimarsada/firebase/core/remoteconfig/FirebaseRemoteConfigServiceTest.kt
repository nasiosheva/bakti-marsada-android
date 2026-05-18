package com.lampung.baktimarsada.firebase.core.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseRemoteConfigServiceTest {

    @Test
    fun `get string prefers tenant specific default when available`() {
        val service = createService(
            defaults = mapOf(
                "app.home_banner_title" to "Global Title",
                "tenant.hkbp-kedaton.app.home_banner_title" to "Tenant Title"
            )
        )

        assertEquals("tenant.hkbp-kedaton.app.home_banner_title", service.getResolvedKey("app.home_banner_title"))
        assertEquals("Tenant Title", service.getString("app.home_banner_title"))
    }

    @Test
    fun `get string falls back to global default when tenant key missing`() {
        val service = createService(
            defaults = mapOf("app.home_banner_title" to "Global Title")
        )

        assertEquals("app.home_banner_title", service.getResolvedKey("app.home_banner_title"))
        assertEquals("Global Title", service.getString("app.home_banner_title"))
    }

    @Test
    fun `typed access reads defaults without fetch`() {
        val service = createService(
            defaults = mapOf(
                "app.home_banner_enabled" to "true",
                "app.minimum_supported_version_code" to "5",
                "app.ratio" to "1.5"
            )
        )

        assertTrue(service.getBoolean("app.home_banner_enabled"))
        assertEquals(5L, service.getLong("app.minimum_supported_version_code"))
        assertEquals(1.5, service.getDouble("app.ratio"), 0.0)
    }

    @Test
    fun `json decode returns parsed value when decoder succeeds`() {
        val service = createService(
            defaults = mapOf("app.payload" to """{"enabled":true}""")
        )

        val value = service.getJson("app.payload", decoder = { raw ->
            if (raw == """{"enabled":true}""") SamplePayload(enabled = true) else null
        }, tenantAware = false)

        assertEquals(SamplePayload(enabled = true), value)
    }

    @Test
    fun `json decode returns null when decoder fails`() {
        val service = createService(
            defaults = mapOf("app.payload" to "invalid")
        )

        val value = service.getJson("app.payload", decoder = { null }, tenantAware = false)

        assertNull(value)
    }

    @Test
    fun `fetch and activate configures client with defaults and policy`() = runTest {
        val client = FakeRemoteConfigClient()
        val service = createService(
            defaults = mapOf("app.home_banner_title" to "Hello"),
            client = client,
            fetchPolicy = RemoteConfigFetchPolicy(minimumFetchIntervalSeconds = 60L)
        )

        val fetchResult = service.fetchAndActivate()
        val activateResult = service.activate()

        assertTrue(fetchResult)
        assertTrue(activateResult)
        assertEquals(100, client.configuredDefaultsResId)
        assertEquals(60L, client.configuredFetchIntervalSeconds)
        assertEquals(1, client.fetchCalls)
        assertEquals(1, client.activateCalls)
    }

    @Test
    fun `remote value overrides local default when source is non static`() {
        val client = FakeRemoteConfigClient(
            stringValues = mapOf("tenant.hkbp-kedaton.app.home_banner_title" to "Remote Tenant"),
            sources = mapOf(
                "tenant.hkbp-kedaton.app.home_banner_title" to FirebaseRemoteConfig.VALUE_SOURCE_REMOTE
            )
        )
        val service = createService(
            defaults = mapOf("app.home_banner_title" to "Global Title"),
            client = client
        )

        assertEquals("Remote Tenant", service.getString("app.home_banner_title"))
    }

    @Test
    fun `missing boolean returns false when firebase unavailable`() {
        val service = createService(defaults = emptyMap())

        assertFalse(service.getBoolean("app.missing"))
    }

    private fun createService(
        defaults: Map<String, String>,
        client: FakeRemoteConfigClient = FakeRemoteConfigClient(),
        fetchPolicy: RemoteConfigFetchPolicy = RemoteConfigFetchPolicy(3600L)
    ): FirebaseRemoteConfigService {
        return FirebaseRemoteConfigService(
            client = client,
            defaultsProvider = FakeRemoteConfigDefaultsProvider(defaults),
            resolver = DefaultTenantAwareRemoteConfigResolver(),
            fetchPolicy = fetchPolicy
        )
    }

    private data class SamplePayload(val enabled: Boolean)

    private class FakeRemoteConfigDefaultsProvider(
        private val defaults: Map<String, String>
    ) : RemoteConfigDefaultsProvider {
        override val defaultsResId: Int = 100

        override fun getAll(): Map<String, String> = defaults

        override fun contains(key: String): Boolean = defaults.containsKey(key)

        override fun getString(key: String): String? = defaults[key]
    }

    private class FakeRemoteConfigClient(
        private val stringValues: Map<String, String> = emptyMap(),
        private val sources: Map<String, Int> = emptyMap()
    ) : RemoteConfigClient {
        var configuredDefaultsResId: Int? = null
        var configuredFetchIntervalSeconds: Long? = null
        var fetchCalls: Int = 0
        var activateCalls: Int = 0

        override suspend fun configure(defaultsResId: Int, minimumFetchIntervalSeconds: Long) {
            configuredDefaultsResId = defaultsResId
            configuredFetchIntervalSeconds = minimumFetchIntervalSeconds
        }

        override suspend fun fetchAndActivate(): Boolean {
            fetchCalls += 1
            return true
        }

        override suspend fun activate(): Boolean {
            activateCalls += 1
            return true
        }

        override fun getString(key: String): String = stringValues[key].orEmpty()

        override fun getValueSource(key: String): Int {
            return sources[key] ?: FirebaseRemoteConfig.VALUE_SOURCE_STATIC
        }
    }
}
