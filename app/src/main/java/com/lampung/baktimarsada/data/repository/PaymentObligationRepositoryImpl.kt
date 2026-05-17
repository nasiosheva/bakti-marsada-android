package com.lampung.baktimarsada.data.repository

import androidx.room.withTransaction
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.data.mapper.toDomain
import com.lampung.baktimarsada.data.mapper.toDto
import com.lampung.baktimarsada.data.mapper.toEntity
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.db.AppDatabase
import com.lampung.baktimarsada.db.dao.PaymentObligationDao
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.repository.PaymentObligationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentObligationRepositoryImpl @Inject constructor(
    private val dao: PaymentObligationDao,
    private val database: AppDatabase,
    private val remoteDataSource: AppRemoteDataSource
) : PaymentObligationRepository {

    override fun observeObligations(): Flow<List<PaymentObligationDetail>> {
        return dao.observeAll().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            val items = remoteDataSource.fetchPaymentObligations(sectorContext.sectorId).map { it.toEntity() }
            database.withTransaction {
                dao.clearAll()
                dao.insertAll(items)
            }
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to load payment obligations", throwable) }
        )
    }

    override suspend fun save(obligation: PaymentObligationDetail, sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            remoteDataSource.savePaymentObligation(obligation.toDto())
            refresh(sectorContext)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to save payment obligation", throwable) }
        )
    }

    override suspend fun delete(obligationId: String, sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            remoteDataSource.deletePaymentObligation(obligationId)
            refresh(sectorContext)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to delete payment obligation", throwable) }
        )
    }
}
