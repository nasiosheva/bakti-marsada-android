package com.lampung.baktimarsada.feature.arisan.presentation

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.domain.model.PaymentStatus
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.repository.ArisanParticipantRepository
import com.lampung.baktimarsada.repository.MemberRepository
import com.lampung.baktimarsada.repository.PaymentObligationRepository
import com.lampung.baktimarsada.test.MainDispatcherRule
import com.lampung.baktimarsada.test.fakes.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArisanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `refresh loads selected participants into state`() = runTest {
        val memberRepository = FakeMemberRepository()
        val paymentRepository = FakePaymentRepository()
        val participantRepository = FakeArisanParticipantRepository(listOf("member-1", "member-2"))
        val viewModel = ArisanViewModel(
            arisanParticipantRepository = participantRepository,
            memberRepository = memberRepository,
            paymentRepository = paymentRepository,
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)
        )

        viewModel.refresh(SectorContext("sector-1", "Sektor 1"))
        advanceUntilIdle()

        assertEquals(setOf("member-1", "member-2"), viewModel.state.value.selectedParticipantIds)
        assertEquals(2, viewModel.state.value.group?.participants?.size)
    }

    @Test
    fun `setWinner updates winner and message`() = runTest {
        val viewModel = ArisanViewModel(
            arisanParticipantRepository = FakeArisanParticipantRepository(emptyList()),
            memberRepository = FakeMemberRepository(),
            paymentRepository = FakePaymentRepository(),
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)
        )

        viewModel.setWinner("P. Simanjuntak")
        advanceUntilIdle()

        assertEquals("P. Simanjuntak", viewModel.state.value.winnerName)
        assertTrue(viewModel.state.value.message?.contains("Pemenang arisan") == true)
    }

    private class FakeArisanParticipantRepository(
        private var selectedIds: List<String>
    ) : ArisanParticipantRepository {
        override suspend fun fetch(sectorContext: SectorContext): AppResult<List<String>> = AppResult.Success(selectedIds)

        override suspend fun replace(sectorContext: SectorContext, memberIds: List<String>): AppResult<List<String>> {
            selectedIds = memberIds
            return AppResult.Success(selectedIds)
        }

        override suspend fun fillAllFromMembers(sectorContext: SectorContext): AppResult<List<String>> {
            return AppResult.Success(selectedIds)
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
                    roleInSector = "Jemaat",
                    sectorId = "sector-1",
                    sectorName = "Sektor 1"
                ),
                MemberDetail(
                    id = "member-2",
                    fullName = "S. Sihombing",
                    familyGroup = "Keluarga Sihombing",
                    phoneNumber = "0813",
                    address = "Jl. Mawar",
                    roleInSector = "Jemaat",
                    sectorId = "sector-1",
                    sectorName = "Sektor 1"
                )
            )
        )

        override fun observeMembers(): Flow<List<MemberDetail>> = members
        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun save(member: MemberDetail, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun delete(memberId: String, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakePaymentRepository : PaymentObligationRepository {
        private val items = MutableStateFlow(
            listOf(
                PaymentObligationDetail(
                    id = "payment-1",
                    memberId = "member-1",
                    memberName = "P. Simanjuntak",
                    title = "Iuran Arisan Mei 2026",
                    description = "Iuran arisan",
                    amount = 100_000,
                    dueDate = "2026-05-30",
                    status = PaymentStatus.PAID,
                    sectorId = "sector-1",
                    sectorName = "Sektor 1"
                )
            )
        )

        override fun observeObligations(): Flow<List<PaymentObligationDetail>> = items
        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun save(obligation: PaymentObligationDetail, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun delete(obligationId: String, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
