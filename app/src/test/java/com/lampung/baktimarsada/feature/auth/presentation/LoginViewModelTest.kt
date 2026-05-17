package com.lampung.baktimarsada.feature.auth.presentation

import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.core.resources.StringProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.domain.repository.AuthRepository
import com.lampung.baktimarsada.domain.usecase.LoginUseCase
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

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `submitWithDemoAdmin logs in with admin sample account`() = runTest {
        val repository = FakeAuthRepository(role = UserRole.ADMIN)
        val viewModel = createViewModel(repository, FakeRemoteDataSource())

        viewModel.submitWithDemoAdmin()
        advanceUntilIdle()

        assertEquals(AppConstants.SAMPLE_ADMIN_IDENTIFIER, repository.lastIdentifier)
        assertEquals(AppConstants.SAMPLE_ADMIN_PASSWORD, repository.lastPassword)
        assertEquals(UserRole.ADMIN, viewModel.state.value.loggedInRole)
    }

    @Test
    fun `submitWithDemoJemaat logs in with jemaat sample account`() = runTest {
        val repository = FakeAuthRepository(role = UserRole.JEMAAT)
        val viewModel = createViewModel(repository, FakeRemoteDataSource())

        viewModel.submitWithDemoJemaat()
        advanceUntilIdle()

        assertEquals(AppConstants.SAMPLE_JEMAAT_IDENTIFIER, repository.lastIdentifier)
        assertEquals(AppConstants.SAMPLE_JEMAAT_PASSWORD, repository.lastPassword)
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
        assertEquals(AppConstants.SAMPLE_ADMIN_IDENTIFIER, repository.lastIdentifier)
        assertEquals(AppConstants.SAMPLE_ADMIN_PASSWORD, repository.lastPassword)
        assertEquals(UserRole.ADMIN, viewModel.state.value.loggedInRole)
    }

    private fun createViewModel(
        repository: FakeAuthRepository,
        remoteDataSource: FakeRemoteDataSource
    ): LoginViewModel {
        return LoginViewModel(
            loginUseCase = LoginUseCase(repository),
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher),
            stringProvider = FakeStringProvider(),
            remoteDataSource = remoteDataSource
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
                else -> "string-$resId"
            }
        }

        override fun get(resId: Int, vararg args: Any): String = get(resId)
    }

    private class FakeRemoteDataSource : AppRemoteDataSource {
        var resetCalls: Int = 0

        override suspend fun login(request: LoginRequestDto): SessionResponseDto = unsupported()
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
        override suspend fun resetSimulationData() {
            resetCalls += 1
        }

        private fun unsupported(): Nothing {
            error("unsupported in this test")
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
