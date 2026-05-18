package com.lampung.baktimarsada.feature.members.presentation

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.repository.MemberRepository
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MemberViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `refresh populates members from repository`() = runTest {
        val repository = FakeMemberRepository()
        val authRepository = FakeAuthRepository(sampleSession())
        val viewModel = MemberViewModel(
            repository = repository,
            authRepository = authRepository,
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)
        )

        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.items.size)
        assertEquals("Boru Simanjuntak", viewModel.state.value.items.first().fullName)
        assertEquals("sector-1", repository.lastSectorId)
        assertEquals(1, repository.refreshCalls)
    }

    @Test
    fun `delete removes member from state`() = runTest {
        val repository = FakeMemberRepository()
        val authRepository = FakeAuthRepository(sampleSession())
        val viewModel = MemberViewModel(
            repository = repository,
            authRepository = authRepository,
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)
        )
        advanceUntilIdle()

        val id = viewModel.state.value.items.first().id
        viewModel.delete(id)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.items.isEmpty())
    }

    private class FakeMemberRepository : MemberRepository {
        private val items = MutableStateFlow(emptyList<MemberDetail>())
        var refreshCalls: Int = 0
        var lastSectorId: String? = null

        override fun observeMembers(): Flow<List<MemberDetail>> = items

        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> {
            refreshCalls += 1
            lastSectorId = sectorContext.sectorId
            items.value = listOf(
                MemberDetail(
                    id = "member-1",
                    fullName = "Boru Simanjuntak",
                    familyGroup = "Keluarga Simanjuntak",
                    phoneNumber = "081234567890",
                    address = "Jl. Wijk Simulasi",
                    roleInSector = "Bendahara",
                    sectorId = sectorContext.sectorId,
                    sectorName = sectorContext.sectorName
                )
            )
            return AppResult.Success(Unit)
        }

        override suspend fun save(member: MemberDetail, sectorContext: SectorContext): AppResult<Unit> {
            items.value = listOf(member)
            return AppResult.Success(Unit)
        }

        override suspend fun delete(memberId: String, sectorContext: SectorContext): AppResult<Unit> {
            items.value = items.value.filterNot { it.id == memberId }
            return AppResult.Success(Unit)
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
