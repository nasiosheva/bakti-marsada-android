package com.lampung.baktimarsada.feature.dashboard.presentation

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.model.FinanceReportDetail
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.domain.model.PaymentStatus
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.repository.EventRepository
import com.lampung.baktimarsada.repository.FinanceReportRepository
import com.lampung.baktimarsada.repository.MemberRepository
import com.lampung.baktimarsada.repository.PaymentObligationRepository
import com.lampung.baktimarsada.test.MainDispatcherRule
import com.lampung.baktimarsada.test.fakes.FakeAuthRepository
import com.lampung.baktimarsada.test.fakes.TestDispatcherProvider
import com.lampung.baktimarsada.test.fakes.UnsupportedAppRemoteDataSource
import com.lampung.baktimarsada.test.fakes.sampleSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminDashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `resetSimulationData refreshes all modules after reset success`() = runTest {
        val authRepository = FakeAuthRepository(sampleSession())
        val eventRepository = FakeEventRepository()
        val memberRepository = FakeMemberRepository()
        val financeRepository = FakeFinanceRepository()
        val paymentRepository = FakePaymentRepository()
        val remoteDataSource = FakeRemoteDataSource(shouldFailReset = false)

        val viewModel = AdminDashboardViewModel(
            authRepository = authRepository,
            eventRepository = eventRepository,
            memberRepository = memberRepository,
            financeReportRepository = financeRepository,
            paymentObligationRepository = paymentRepository,
            remoteDataSource = remoteDataSource,
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)
        )
        advanceUntilIdle()

        viewModel.resetSimulationData()
        advanceUntilIdle()

        assertEquals(1, remoteDataSource.resetCalls)
        assertEquals(2, eventRepository.refreshCalls)
        assertEquals(2, memberRepository.refreshCalls)
        assertEquals(2, financeRepository.refreshCalls)
        assertEquals(2, paymentRepository.refreshCalls)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `resetSimulationData sets error and skips refresh on reset failure`() = runTest {
        val authRepository = FakeAuthRepository(sampleSession())
        val eventRepository = FakeEventRepository()
        val memberRepository = FakeMemberRepository()
        val financeRepository = FakeFinanceRepository()
        val paymentRepository = FakePaymentRepository()
        val remoteDataSource = FakeRemoteDataSource(shouldFailReset = true)

        val viewModel = AdminDashboardViewModel(
            authRepository = authRepository,
            eventRepository = eventRepository,
            memberRepository = memberRepository,
            financeReportRepository = financeRepository,
            paymentObligationRepository = paymentRepository,
            remoteDataSource = remoteDataSource,
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)
        )
        advanceUntilIdle()

        viewModel.resetSimulationData()
        advanceUntilIdle()

        assertEquals(1, remoteDataSource.resetCalls)
        assertEquals(1, eventRepository.refreshCalls)
        assertEquals(1, memberRepository.refreshCalls)
        assertEquals(1, financeRepository.refreshCalls)
        assertEquals(1, paymentRepository.refreshCalls)
        assertEquals("reset failed", viewModel.state.value.errorMessage)
    }

    private class FakeEventRepository : EventRepository {
        private val items = MutableStateFlow(
            listOf(
                EventDetail("event-1", "Event", "Desc", "2026-05-21", "Lokasi", "sector-1", "Sektor 1")
            )
        )
        var refreshCalls: Int = 0
        override fun observeEvents(): Flow<List<EventDetail>> = items
        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> {
            refreshCalls += 1
            return AppResult.Success(Unit)
        }
        override suspend fun save(event: EventDetail, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun delete(eventId: String, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeMemberRepository : MemberRepository {
        private val items = MutableStateFlow(
            listOf(
                MemberDetail("member-1", "Member", "Keluarga", "0812", "Alamat", "Jemaat", "sector-1", "Sektor 1")
            )
        )
        var refreshCalls: Int = 0
        override fun observeMembers(): Flow<List<MemberDetail>> = items
        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> {
            refreshCalls += 1
            return AppResult.Success(Unit)
        }
        override suspend fun save(member: MemberDetail, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun delete(memberId: String, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeFinanceRepository : FinanceReportRepository {
        private val items = MutableStateFlow(
            listOf(
                FinanceReportDetail("finance-1", "Kas", "Desc", "Mei 2026", 1000, true, "sector-1", "Sektor 1")
            )
        )
        var refreshCalls: Int = 0
        override fun observeReports(): Flow<List<FinanceReportDetail>> = items
        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> {
            refreshCalls += 1
            return AppResult.Success(Unit)
        }
        override suspend fun save(report: FinanceReportDetail, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun delete(reportId: String, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun toggleVisibility(report: FinanceReportDetail, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakePaymentRepository : PaymentObligationRepository {
        private val items = MutableStateFlow(
            listOf(
                PaymentObligationDetail(
                    "payment-1",
                    "member-1",
                    "Member",
                    "Iuran",
                    "Desc",
                    1000,
                    "2026-05-30",
                    PaymentStatus.UNPAID,
                    "sector-1",
                    "Sektor 1"
                )
            )
        )
        var refreshCalls: Int = 0
        override fun observeObligations(): Flow<List<PaymentObligationDetail>> = items
        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> {
            refreshCalls += 1
            return AppResult.Success(Unit)
        }
        override suspend fun save(obligation: PaymentObligationDetail, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun delete(obligationId: String, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeRemoteDataSource(
        private val shouldFailReset: Boolean
    ) : UnsupportedAppRemoteDataSource() {
        var resetCalls: Int = 0
        override suspend fun resetSimulationData() {
            resetCalls += 1
            if (shouldFailReset) error("reset failed")
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
