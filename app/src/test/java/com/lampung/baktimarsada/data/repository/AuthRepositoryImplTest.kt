package com.lampung.baktimarsada.data.repository

import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.datasource.simulate.SimulateAppRemoteDataSource
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.security.SecureStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {
    private val tenant = TenantRuntime.current

    @Test
    fun `login persists session and bootstrap restores it`() = runTest {
        val storage = FakeSecureStorage()
        val repository = AuthRepositoryImpl(
            secureStorage = storage,
            remoteDataSource = SimulateAppRemoteDataSource()
        )

        val result = repository.login(
            tenant.sampleAdminIdentifier,
            tenant.sampleAdminPassword
        )

        assertTrue(result is AppResult.Success)
        val session = (result as AppResult.Success).data
        assertEquals(UserRole.ADMIN, session.role)
        assertEquals(session.authToken, storage.getString(AppConstants.KEY_AUTH_TOKEN))
        assertEquals(session.userId, storage.getString(AppConstants.KEY_USER_ID))
        assertEquals(session.role.name, storage.getString(AppConstants.KEY_USER_ROLE))

        val restored = repository.bootstrapSession()
        assertEquals(session, restored)
    }

    @Test
    fun `logout clears stored session`() = runTest {
        val storage = FakeSecureStorage()
        val repository = AuthRepositoryImpl(
            secureStorage = storage,
            remoteDataSource = SimulateAppRemoteDataSource()
        )
        repository.login(
            tenant.sampleJemaatIdentifier,
            tenant.sampleJemaatPassword
        )

        repository.logout()

        assertNull(storage.getString(AppConstants.KEY_AUTH_TOKEN))
        assertNull(repository.getCurrentSession())
    }

    private class FakeSecureStorage : SecureStorage {
        private val values = linkedMapOf<String, String>()

        override fun putString(key: String, value: String) {
            values[key] = value
        }

        override fun getString(key: String): String? = values[key]

        override fun remove(key: String) {
            values.remove(key)
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
