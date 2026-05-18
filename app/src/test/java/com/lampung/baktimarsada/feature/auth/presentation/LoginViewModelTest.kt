package com.lampung.baktimarsada.feature.auth.presentation

import android.content.Intent
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.core.resources.StringProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.firebase.core.auth.GoogleSignInHelper
import com.lampung.baktimarsada.firebase.core.auth.GoogleSignInToken
import com.lampung.baktimarsada.firebase.notification.NotificationHelper
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountRequestDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountResponseDto
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
import com.lampung.baktimarsada.network.dto.WorshipTemplateDto
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.TenantContext
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.repository.AuthRepository
import com.lampung.baktimarsada.domain.usecase.LoginUseCase
import com.lampung.baktimarsada.domain.usecase.LoginWithGoogleUseCase
import com.lampung.baktimarsada.security.SecureStorage
import com.lampung.baktimarsada.test.MainDispatcherRule
import com.lampung.baktimarsada.test.fakes.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
        val repository = FakeAuthRepository(role = UserRole.ADMIN)
        val viewModel = createViewModel(repository, FakeRemoteDataSource())

        viewModel.submitWithDemoAdmin()
        advanceUntilIdle()

        assertEquals(tenant.sampleAdminIdentifier, repository.lastIdentifier)
        assertEquals(tenant.sampleAdminPassword, repository.lastPassword)
        assertEquals(UserRole.ADMIN, viewModel.state.value.loggedInRole)
    }

    @Test
    fun `submitWithDemoJemaat logs in with jemaat sample account`() = runTest {
        val repository = FakeAuthRepository(role = UserRole.JEMAAT)
        val viewModel = createViewModel(repository, FakeRemoteDataSource())

        viewModel.submitWithDemoJemaat()
        advanceUntilIdle()

        assertEquals(tenant.sampleJemaatIdentifier, repository.lastIdentifier)
        assertEquals(tenant.sampleJemaatPassword, repository.lastPassword)
        assertEquals(UserRole.JEMAAT, viewModel.state.value.loggedInRole)
    }

    @Test
    fun `resetSimulationAndLoginAsAdmin resets seed and logs in as admin`() = runTest {
        val repository = FakeAuthRepository(role = UserRole.ADMIN)
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
        val repository = FakeAuthRepository(role = UserRole.JEMAAT)
        val notificationHelper = FakeNotificationHelper()
        val viewModel = createViewModel(repository, FakeRemoteDataSource(), notificationHelper)

        viewModel.onGoogleSignInResult(Intent("google-sign-in"))
        advanceUntilIdle()

        assertEquals("google", repository.lastIdentifier)
        assertEquals("google-token", repository.lastPassword)
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

    private class FakeAuthRepository(
        private val role: UserRole
    ) : AuthRepository {
        var lastIdentifier: String? = null
        var lastPassword: String? = null

        private val sessionState = MutableStateFlow<SessionState?>(null)

        override fun observeSession(): Flow<SessionState?> = sessionState

        override suspend fun bootstrapSession(): SessionState? = sessionState.value

        override suspend fun getCurrentSession(): SessionState? = sessionState.value

        override suspend fun login(identifier: String, password: String): AppResult<SessionState> {
            lastIdentifier = identifier
            lastPassword = password
            val session = SessionState(
                authToken = "token-$identifier",
                userId = "user-$identifier",
                displayName = "Demo User",
                role = role,
                tenantContext = TenantContext(
                    tenantId = "hkbp",
                    tenantName = "HKBP",
                    subTenantId = "hkbp-kedaton",
                    subTenantName = "HKBP Kedaton"
                ),
                sectorContext = SectorContext(
                    sectorId = "wijk-1",
                    sectorName = "Wijk Simulasi"
                )
            )
            sessionState.value = session
            return AppResult.Success(session)
        }

        override suspend fun loginWithGoogle(idToken: String): AppResult<SessionState> {
            lastIdentifier = "google"
            lastPassword = idToken
            val session = SessionState(
                authToken = "token-google",
                userId = "user-google",
                displayName = "Google User",
                role = role,
                tenantContext = TenantContext(
                    tenantId = "hkbp",
                    tenantName = "HKBP",
                    subTenantId = "hkbp-kedaton",
                    subTenantName = "HKBP Kedaton"
                ),
                sectorContext = SectorContext(
                    sectorId = "wijk-1",
                    sectorName = "Wijk Simulasi"
                )
            )
            sessionState.value = session
            return AppResult.Success(session)
        }

        override suspend fun logout() {
            sessionState.value = null
        }

        override suspend fun syncFcmToken(token: String): AppResult<Unit> = AppResult.Success(Unit)
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

    private class FakeRemoteDataSource : AppRemoteDataSource {
        var resetCalls: Int = 0

        override suspend fun login(request: LoginRequestDto): SessionResponseDto = unsupported()
        override suspend fun loginWithGoogle(request: com.lampung.baktimarsada.network.dto.GoogleLoginRequestDto): SessionResponseDto = unsupported()
        override suspend fun logout(token: String) = Unit
        override suspend fun syncFcmToken(token: String) = Unit
        override suspend fun fetchEvents(sectorId: String): List<EventDto> = unsupported()
        override suspend fun saveEvent(event: EventDto): EventDto = unsupported()
        override suspend fun deleteEvent(eventId: String): Unit = unsupported()
        override suspend fun fetchMembers(sectorId: String): List<MemberDto> = unsupported()
        override suspend fun saveMember(member: MemberDto): MemberDto = unsupported()
        override suspend fun deleteMember(memberId: String): Unit = unsupported()
        override suspend fun fetchFinanceReports(sectorId: String): List<FinanceReportDto> = unsupported()
        override suspend fun saveFinanceReport(report: FinanceReportDto): FinanceReportDto = unsupported()
        override suspend fun deleteFinanceReport(reportId: String): Unit = unsupported()
        override suspend fun fetchPaymentObligations(sectorId: String): List<PaymentObligationDto> = unsupported()
        override suspend fun savePaymentObligation(obligation: PaymentObligationDto): PaymentObligationDto = unsupported()
        override suspend fun deletePaymentObligation(obligationId: String): Unit = unsupported()
        override suspend fun fetchWorshipTemplates(sectorId: String): List<WorshipTemplateDto> = unsupported()
        override suspend fun saveWorshipTemplate(template: WorshipTemplateDto): WorshipTemplateDto = unsupported()
        override suspend fun deleteWorshipTemplate(templateId: String): Unit = unsupported()
        override suspend fun createUserAccount(request: CreateUserAccountRequestDto): CreateUserAccountResponseDto = unsupported()
        override suspend fun fetchUsersByRole(role: String): List<CreateUserAccountResponseDto> = unsupported()
        override suspend fun resetSimulationData() {
            resetCalls += 1
        }

        private fun unsupported(): Nothing {
            error("unsupported in this test")
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

    private class FakeNotificationHelper : NotificationHelper {
        var lastAccountLabel: String? = null

        override fun showNotification(message: com.google.firebase.messaging.RemoteMessage) = Unit

        override fun showGoogleWelcomeNotification(accountLabel: String) {
            lastAccountLabel = accountLabel
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
