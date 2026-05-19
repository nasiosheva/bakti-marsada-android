package com.lampung.baktimarsada.feature.finance.presentation

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.model.FinanceReportDetail
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.repository.FinanceReportRepository
import com.lampung.baktimarsada.test.MainDispatcherRule
import com.lampung.baktimarsada.test.fakes.FakeAuthRepository
import com.lampung.baktimarsada.test.fakes.TestDispatcherProvider
import com.lampung.baktimarsada.test.fakes.sampleSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `toggle visibility updates current report`() = runTest {
        val repository = FakeFinanceRepository()
        val authRepository = FakeAuthRepository(sampleSession())
        val viewModel = FinanceViewModel(
            repository = repository,
            authRepository = authRepository,
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)
        )
        advanceUntilIdle()

        val report = viewModel.state.value.items.first()
        assertTrue(report.isVisibleToJemaat)

        viewModel.onEvent(FinanceEvent.ToggleVisibility(report))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.items.first().isVisibleToJemaat)
    }

    private class FakeFinanceRepository : FinanceReportRepository {
        private val items = MutableStateFlow(
            listOf(
                FinanceReportDetail(
                    id = "finance-1",
                    title = "Kas Mei",
                    description = "Laporan bulanan",
                    periodLabel = "Mei 2026",
                    amount = 1000000,
                    isVisibleToJemaat = true,
                    sectorId = "sector-1",
                    sectorName = "Sektor 1 HKBP"
                )
            )
        )

        override fun observeReports(): Flow<List<FinanceReportDetail>> = items

        override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun save(report: FinanceReportDetail, sectorContext: SectorContext): AppResult<Unit> {
            items.value = listOf(report)
            return AppResult.Success(Unit)
        }

        override suspend fun delete(reportId: String, sectorContext: SectorContext): AppResult<Unit> {
            items.value = emptyList()
            return AppResult.Success(Unit)
        }

        override suspend fun toggleVisibility(report: FinanceReportDetail, sectorContext: SectorContext): AppResult<Unit> {
            items.value = listOf(report.copy(isVisibleToJemaat = !report.isVisibleToJemaat))
            return AppResult.Success(Unit)
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
