package com.lampung.baktimarsada.feature.payments.presentation

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.domain.model.PaymentStatus
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.repository.MemberRepository
import com.lampung.baktimarsada.domain.repository.PaymentObligationRepository
import com.lampung.baktimarsada.test.MainDispatcherRule
import com.lampung.baktimarsada.test.fakes.FakeAuthRepository
import com.lampung.baktimarsada.test.fakes.TestDispatcherProvider
import com.lampung.baktimarsada.test.fakes.sampleSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `refresh exposes payment items and member options`() = runTest {
        val paymentRepository = FakePaymentRepository()
        val memberRepository = FakeMemberRepository()
        val authRepository = FakeAuthRepository(sampleSession())
        val viewModel = PaymentViewModel(
            repository = paymentRepository,
            memberRepository = memberRepository,
            authRepository = authRepository,
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)
        )

        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.items.size)
        assertEquals(1, viewModel.state.value.members.size)
        assertEquals("P. Simanjuntak", viewModel.state.value.members.first().fullName)
    }

    private class FakePaymentRepository : PaymentObligationRepository {
        private val items = MutableStateFlow(
            listOf(
                PaymentObligationDetail(
                    id = "payment-1",
                    memberId = "member-1",
                    memberName = "P. Simanjuntak",
                    title = "Iuran Mei",
                    description = "Iuran bulanan",
                    amount = 50000,
                    dueDate = "2026-05-25",
                    status = PaymentStatus.UNPAID,
                    sectorId = "sector-1",
                    sectorName = "Sektor 1 HKBP"
                )
            )
        )

        override fun observeObligations(): Flow<List<PaymentObligationDetail>> = items

        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun save(obligation: PaymentObligationDetail, sectorContext: SectorContext): AppResult<Unit> {
            items.value = listOf(obligation)
            return AppResult.Success(Unit)
        }

        override suspend fun delete(obligationId: String, sectorContext: SectorContext): AppResult<Unit> {
            items.value = items.value.filterNot { it.id == obligationId }
            return AppResult.Success(Unit)
        }
    }

    private class FakeMemberRepository : MemberRepository {
        private val members = MutableStateFlow(
            listOf(
                MemberDetail(
                    id = "member-1",
                    fullName = "P. Simanjuntak",
                    familyGroup = "Keluarga Simanjuntak",
                    phoneNumber = "0812",
                    address = "Jl. Melati",
                    roleInSector = "Penatua",
                    sectorId = "sector-1",
                    sectorName = "Sektor 1 HKBP"
                )
            )
        )

        override fun observeMembers(): Flow<List<MemberDetail>> = members

        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun save(member: MemberDetail, sectorContext: SectorContext): AppResult<Unit> {
            members.value = listOf(member)
            return AppResult.Success(Unit)
        }

        override suspend fun delete(memberId: String, sectorContext: SectorContext): AppResult<Unit> {
            members.value = members.value.filterNot { it.id == memberId }
            return AppResult.Success(Unit)
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
