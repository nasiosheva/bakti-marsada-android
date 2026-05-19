package com.lampung.baktimarsada.feature.events.presentation

import androidx.lifecycle.SavedStateHandle
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.feature.app.navigation.AppRoutes
import com.lampung.baktimarsada.repository.EventRepository
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
class EventDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `refresh loads item by eventId from repository flow`() = runTest {
        val repository = FakeEventRepository()
        val viewModel = EventDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf(AppRoutes.EVENT_ID_ARG to "event-2")),
            repository = repository,
            authRepository = FakeAuthRepository(sampleSession()),
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)
        )

        advanceUntilIdle()

        assertEquals("event-2", viewModel.state.value.item?.id)
        assertEquals("Partangiangan Keluarga", viewModel.state.value.item?.title)
    }

    private class FakeEventRepository : EventRepository {
        private val items = MutableStateFlow(emptyList<EventDetail>())

        override fun observeEvents(): Flow<List<EventDetail>> = items

        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> {
            items.value = listOf(
                EventDetail(
                    id = "event-1",
                    title = "Partangiangan Rabu",
                    description = "Deskripsi 1",
                    scheduledAt = "2026-05-20 19:00",
                    location = "Rumah A",
                    sectorId = sectorContext.sectorId,
                    sectorName = sectorContext.sectorName
                ),
                EventDetail(
                    id = "event-2",
                    title = "Partangiangan Keluarga",
                    description = "Deskripsi 2",
                    scheduledAt = "2026-05-21 19:00",
                    location = "Rumah B",
                    sectorId = sectorContext.sectorId,
                    sectorName = sectorContext.sectorName
                )
            )
            return AppResult.Success(Unit)
        }

        override suspend fun save(event: EventDetail, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun delete(eventId: String, sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
