package com.lampung.baktimarsada.repository

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.model.FinanceReportDetail
import com.lampung.baktimarsada.domain.model.SectorContext
import kotlinx.coroutines.flow.Flow

interface FinanceReportRepository {
    fun observeReports(): Flow<List<FinanceReportDetail>>
    suspend fun refresh(sectorContext: SectorContext): AppResult<Unit>
    suspend fun save(report: FinanceReportDetail, sectorContext: SectorContext): AppResult<Unit>
    suspend fun delete(reportId: String, sectorContext: SectorContext): AppResult<Unit>
    suspend fun toggleVisibility(report: FinanceReportDetail, sectorContext: SectorContext): AppResult<Unit>
}

// created by Mories Deo Hutapea, S.E.,S.Kom
