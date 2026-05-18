package com.lampung.baktimarsada.feature.auth.presentation

import android.content.Intent
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.core.resources.StringProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.firebase.core.auth.GoogleSignInHelper
import com.lampung.baktimarsada.firebase.core.auth.GoogleSignInToken
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.TenantContext
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.domain.usecase.LoginUseCase
import com.lampung.baktimarsada.domain.usecase.LoginWithGoogleUseCase
import com.lampung.baktimarsada.security.SecureStorage
import com.lampung.baktimarsada.test.MainDispatcherRule
import com.lampung.baktimarsada.test.fakes.FakeAuthRepository
import com.lampung.baktimarsada.test.fakes.FakeNotificationHelper
import com.lampung.baktimarsada.test.fakes.TestDispatcherProvider
import com.lampung.baktimarsada.test.fakes.UnsupportedAppRemoteDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val tenant = TenantRuntime.current

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `submitWithDemoAdmin logs in with admin sample account`() = runTest {
        val repository = FakeAuthRepository(fixedRole = UserRole.ADMIN)
        val viewModel = createViewModel(repository, FakeRemoteDataSource())

        viewModel.submitWithDemoAdmin()
        advanceUntilIdle()

        assertEquals(tenant.sampleAdminIdentifier, repository.lastIdentifier)
        assertEquals(tenant.sampleAdminPassword, repository.lastPassword)
        assertEquals(UserRole.ADMIN, viewModel.state.value.loggedInRole)
    }

    @Test
    fun `submitWithDemoJemaat logs in with jemaat sample account`() = runTest {
        val repository = FakeAuthRepository(fixedRole = UserRole.JEMAAT)
        val viewModel = createViewModel(repository, FakeRemoteDataSource())

        viewModel.submitWithDemoJemaat()
        advanceUntilIdle()

        assertEquals(tenant.sampleJemaatIdentifier, repository.lastIdentifier)
        assertEquals(tenant.sampleJemaatPassword, repository.lastPassword)
        assertEquals(UserRole.JEMAAT, viewModel.state.value.loggedInRole)
    }

    @Test
    fun `resetSimulationAndLoginAsAdmin resets seed and logs in as admin`() = runTest {
        val repository = FakeAuthRepository(fixedRole = UserRole.ADMIN)
        val remoteDataSource = FakeRemoteDataSource()
        val viewModel = createViewModel(repository, remoteDataSource)

        viewModel.resetSimulationAndLoginAsAdmin()
        advanceUntilIdle()

        assertEquals(1, remoteDataSource.resetCalls)
        assertEquals(tenant.sampleAdminIdentifier, repository.lastIdentifier)
        assertEquals(tenant.sampleAdminPassword, repository.lastPassword)
        assertEquals(UserRole.ADMIN, viewModel.state.value.loggedInRole)
    }

    @Test
    fun `google sign in result logs in with google token`() = runTest {
        val repository = FakeAuthRepository(fixedRole = UserRole.JEMAAT)
        val notificationHelper = FakeNotificationHelper()
        val viewModel = createViewModel(repository, FakeRemoteDataSource(), notificationHelper)

        viewModel.onGoogleSignInResult(Intent("google-sign-in"))
        advanceUntilIdle()

        assertEquals("google-token", repository.lastGoogleToken)
        assertEquals(UserRole.JEMAAT, viewModel.state.value.loggedInRole)
        assertEquals("google@example.com", notificationHelper.lastAccountLabel)
    }

    private fun createViewModel(
        repository: FakeAuthRepository,
        remoteDataSource: FakeRemoteDataSource,
        notificationHelper: FakeNotificationHelper = FakeNotificationHelper()
    ): LoginViewModel {
        return LoginViewModel(
            loginUseCase = LoginUseCase(repository),
            loginWithGoogleUseCase = LoginWithGoogleUseCase(repository),
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher),
            stringProvider = FakeStringProvider(),
            remoteDataSource = remoteDataSource,
            secureStorage = FakeSecureStorage(),
            googleSignInHelper = FakeGoogleSignInHelper(),
            notificationHelper = notificationHelper
        )
    }

    private class FakeStringProvider : StringProvider {
        override fun get(resId: Int): String {
            return when (resId) {
                R.string.login_error_identifier_required -> "Identifier wajib diisi"
                R.string.login_error_password_required -> "Password wajib diisi"
                R.string.login_error_simulation_reset_failed -> "Gagal mereset data simulasi"
                R.string.login_google_unavailable -> "Google Sign-In belum dikonfigurasi untuk aplikasi ini."
                R.string.login_google_failed -> "Google Sign-In gagal diproses."
                else -> "string-$resId"
            }
        }

        override fun get(resId: Int, vararg args: Any): String = get(resId)
    }

    private class FakeRemoteDataSource : UnsupportedAppRemoteDataSource() {
        var resetCalls: Int = 0

        override suspend fun resetSimulationData() {
            resetCalls += 1
        }
    }

    private class FakeGoogleSignInHelper : GoogleSignInHelper {
        override fun isAvailable(): Boolean = true

        override fun createSignInIntent(): Intent? = Intent("google-sign-in")

        override suspend fun extractToken(data: Intent?): Result<GoogleSignInToken> {
            return Result.success(
                GoogleSignInToken(
                    idToken = "google-token",
                    email = "google@example.com",
                    displayName = "Google User"
                )
            )
        }
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
