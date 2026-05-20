package com.lampung.baktimarsada.data.repository

import androidx.room.withTransaction
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.data.mapper.toDomain
import com.lampung.baktimarsada.data.mapper.toDto
import com.lampung.baktimarsada.data.mapper.toEntity
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.db.AppDatabase
import com.lampung.baktimarsada.db.dao.FinanceReportDao
import com.lampung.baktimarsada.model.FinanceReportDetail
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.repository.AuthRepository
import com.lampung.baktimarsada.repository.FinanceReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceReportRepositoryImpl @Inject constructor(
    private val dao: FinanceReportDao,
    private val database: AppDatabase,
    private val remoteDataSource: AppRemoteDataSource,
    private val authRepository: AuthRepository
) : FinanceReportRepository {

    private suspend fun currentTenantId(): String =
        authRepository.getCurrentSession()?.tenantContext?.tenantId.orEmpty()

    override fun observeReports(): Flow<List<FinanceReportDetail>> {
        return dao.observeAll().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            val tenantId = currentTenantId()
            val items = remoteDataSource.fetchFinanceReports(tenantId, sectorContext.sectorId).map { it.toEntity() }
            database.withTransaction {
                dao.clearAll()
                dao.insertAll(items)
            }
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to load finance reports", throwable) }
        )
    }

    override suspend fun save(report: FinanceReportDetail, sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            val tenantId = currentTenantId()
            remoteDataSource.saveFinanceReport(tenantId, report.toDto())
            refresh(sectorContext)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to save finance report", throwable) }
        )
    }

    override suspend fun delete(reportId: String, sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            val tenantId = currentTenantId()
            remoteDataSource.deleteFinanceReport(tenantId, reportId)
            refresh(sectorContext)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to delete finance report", throwable) }
        )
    }

    override suspend fun toggleVisibility(report: FinanceReportDetail, sectorContext: SectorContext): AppResult<Unit> {
        return save(report.copy(isVisibleToJemaat = !report.isVisibleToJemaat), sectorContext)
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
