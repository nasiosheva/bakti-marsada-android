package com.lampung.baktimarsada.feature.app.presentation

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.FinanceReportDetail
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.domain.usecase.BootstrapSessionUseCase
import com.lampung.baktimarsada.domain.usecase.LogoutUseCase
import com.lampung.baktimarsada.domain.usecase.ObserveSessionUseCase
import com.lampung.baktimarsada.feature.app.navigation.AppRoutes
import com.lampung.baktimarsada.repository.EventRepository
import com.lampung.baktimarsada.repository.FinanceReportRepository
import com.lampung.baktimarsada.repository.MemberRepository
import com.lampung.baktimarsada.repository.PaymentObligationRepository
import com.lampung.baktimarsada.test.MainDispatcherRule
import com.lampung.baktimarsada.test.fakes.FakeAuthRepository
import com.lampung.baktimarsada.test.fakes.FakeNotificationHelper
import com.lampung.baktimarsada.test.fakes.sampleSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppEntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `bootstrap without session navigates to login`() = runTest {
        val repository = FakeAuthRepository(initialSession = null)

        val viewModel = AppEntryViewModel(
            observeSessionUseCase = ObserveSessionUseCase(repository),
            bootstrapSessionUseCase = BootstrapSessionUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
            eventRepository = FakeEventRepository(),
            memberRepository = FakeMemberRepository(),
            financeReportRepository = FakeFinanceReportRepository(),
            paymentObligationRepository = FakePaymentObligationRepository(),
            notificationHelper = FakeNotificationHelper()
        )
        advanceUntilIdle()

        assertEquals(AppRoutes.LOGIN, viewModel.state.value.pendingRoute)
        assertNull(viewModel.state.value.session)
    }

    @Test
    fun `bootstrap with admin session navigates to admin home`() = runTest {
        val repository = FakeAuthRepository(initialSession = sampleSession(UserRole.ADMIN))

        val viewModel = AppEntryViewModel(
            observeSessionUseCase = ObserveSessionUseCase(repository),
            bootstrapSessionUseCase = BootstrapSessionUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
            eventRepository = FakeEventRepository(),
            memberRepository = FakeMemberRepository(),
            financeReportRepository = FakeFinanceReportRepository(),
            paymentObligationRepository = FakePaymentObligationRepository(),
            notificationHelper = FakeNotificationHelper()
        )
        advanceUntilIdle()

        assertEquals(AppRoutes.ADMIN_HOME, viewModel.state.value.pendingRoute)
        assertEquals(UserRole.ADMIN, viewModel.state.value.session?.role)
    }

    @Test
    fun `logout clears session and returns to login`() = runTest {
        val repository = FakeAuthRepository(initialSession = sampleSession(UserRole.JEMAAT))
        val viewModel = AppEntryViewModel(
            observeSessionUseCase = ObserveSessionUseCase(repository),
            bootstrapSessionUseCase = BootstrapSessionUseCase(repository),
            logoutUseCase = LogoutUseCase(repository),
            eventRepository = FakeEventRepository(),
            memberRepository = FakeMemberRepository(),
            financeReportRepository = FakeFinanceReportRepository(),
            paymentObligationRepository = FakePaymentObligationRepository(),
            notificationHelper = FakeNotificationHelper()
        )
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        assertEquals(AppRoutes.LOGIN, viewModel.state.value.pendingRoute)
        assertNull(viewModel.state.value.session)
    }

    private class FakeEventRepository : EventRepository {
        override fun observeEvents(): Flow<List<EventDetail>> = flowOf(emptyList())
        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun save(event: EventDetail, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun delete(eventId: String, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeMemberRepository : MemberRepository {
        override fun observeMembers(): Flow<List<MemberDetail>> = flowOf(emptyList())
        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun save(member: MemberDetail, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun delete(memberId: String, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeFinanceReportRepository : FinanceReportRepository {
        override fun observeReports(): Flow<List<FinanceReportDetail>> = flowOf(emptyList())
        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun save(report: FinanceReportDetail, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun delete(reportId: String, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun toggleVisibility(report: FinanceReportDetail, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakePaymentObligationRepository : PaymentObligationRepository {
        override fun observeObligations(): Flow<List<PaymentObligationDetail>> = flowOf(emptyList())
        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun save(obligation: PaymentObligationDetail, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun delete(obligationId: String, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
