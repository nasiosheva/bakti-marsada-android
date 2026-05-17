package com.lampung.baktimarsada.feature.events.presentation

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.WorshipTemplate
import com.lampung.baktimarsada.domain.repository.EventRepository
import com.lampung.baktimarsada.domain.repository.WorshipTemplateRepository
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
class EventViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `refresh populates events from repository`() = runTest {
        val repository = FakeEventRepository()
        val templateRepository = FakeWorshipTemplateRepository()
        val authRepository = FakeAuthRepository(sampleSession())
        val viewModel = EventViewModel(
            repository = repository,
            templateRepository = templateRepository,
            authRepository = authRepository,
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)
        )

        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.items.size)
        assertEquals("Partangiangan Rabu", viewModel.state.value.items.first().title)
        assertEquals("Partangiangan Sektor/Wijk", viewModel.state.value.templates.first().title)
        assertEquals(1, repository.refreshCalls)
        assertEquals(1, templateRepository.refreshCalls)
    }

    @Test
    fun `delete removes item from state`() = runTest {
        val repository = FakeEventRepository()
        val templateRepository = FakeWorshipTemplateRepository()
        val authRepository = FakeAuthRepository(sampleSession())
        val viewModel = EventViewModel(
            repository = repository,
            templateRepository = templateRepository,
            authRepository = authRepository,
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)
        )
        advanceUntilIdle()

        val id = viewModel.state.value.items.first().id
        viewModel.delete(id)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.items.isEmpty())
    }

    @Test
    fun `save template refreshes template cache`() = runTest {
        val repository = FakeEventRepository()
        val templateRepository = FakeWorshipTemplateRepository()
        val authRepository = FakeAuthRepository(sampleSession())
        val viewModel = EventViewModel(
            repository = repository,
            templateRepository = templateRepository,
            authRepository = authRepository,
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)
        )
        advanceUntilIdle()

        viewModel.saveTemplate(
            WorshipTemplate(
                id = "template-2",
                tenantId = "tenant-1",
                sectorId = "sector-1",
                title = "Partangiangan Syukuran",
                description = "Template syukur",
                items = emptyList()
            )
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.templates.size)
        assertEquals(1, templateRepository.saveCalls)
    }

    private class FakeEventRepository : EventRepository {
        private val items = MutableStateFlow(emptyList<EventDetail>())
        var refreshCalls: Int = 0

        override fun observeEvents(): Flow<List<EventDetail>> = items

        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> {
            refreshCalls += 1
            items.value = listOf(
                EventDetail(
                    id = "event-1",
                    title = "Partangiangan Rabu",
                    description = "Renungan tengah minggu",
                    scheduledAt = "2026-05-21 19:00",
                    location = "Rumah Keluarga Simanjuntak",
                    sectorId = sectorContext.sectorId,
                    sectorName = sectorContext.sectorName
                )
            )
            return AppResult.Success(Unit)
        }

        override suspend fun save(event: EventDetail, sectorContext: SectorContext): AppResult<Unit> {
            items.value = listOf(event)
            return AppResult.Success(Unit)
        }

        override suspend fun delete(eventId: String, sectorContext: SectorContext): AppResult<Unit> {
            items.value = items.value.filterNot { it.id == eventId }
            return AppResult.Success(Unit)
        }
    }

    private class FakeWorshipTemplateRepository : WorshipTemplateRepository {
        private val items = MutableStateFlow(emptyList<WorshipTemplate>())
        var refreshCalls: Int = 0
        var saveCalls: Int = 0

        override fun observeTemplates(): Flow<List<WorshipTemplate>> = items

        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> {
            refreshCalls += 1
            if (items.value.isEmpty()) {
                items.value = listOf(
                    WorshipTemplate(
                        id = "template-1",
                        tenantId = "tenant-1",
                        sectorId = sectorContext.sectorId,
                        title = "Partangiangan Sektor/Wijk",
                        description = "Template rutin",
                        items = emptyList()
                    )
                )
            }
            return AppResult.Success(Unit)
        }

        override suspend fun save(template: WorshipTemplate, sectorContext: SectorContext): AppResult<Unit> {
            saveCalls += 1
            items.value = items.value.filterNot { it.id == template.id } + template
            return AppResult.Success(Unit)
        }

        override suspend fun delete(templateId: String, sectorContext: SectorContext): AppResult<Unit> {
            items.value = items.value.filterNot { it.id == templateId }
            return AppResult.Success(Unit)
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
