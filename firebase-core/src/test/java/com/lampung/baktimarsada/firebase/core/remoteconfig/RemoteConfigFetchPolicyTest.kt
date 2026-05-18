package com.lampung.baktimarsada.firebase.core.remoteconfig

import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteConfigFetchPolicyTest {

    @Test
    fun `debug policy uses short fetch interval`() {
        val policy = RemoteConfigFetchPolicy(minimumFetchIntervalSeconds = 60L)

        assertEquals(60L, policy.minimumFetchIntervalSeconds)
    }

    @Test
    fun `release policy uses conservative fetch interval`() {
        val policy = RemoteConfigFetchPolicy(minimumFetchIntervalSeconds = 3600L)

        assertEquals(3600L, policy.minimumFetchIntervalSeconds)
    }
}
