package com.lampung.baktimarsada.data.repository

import androidx.room.withTransaction
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.data.mapper.toDomain
import com.lampung.baktimarsada.data.mapper.toDto
import com.lampung.baktimarsada.data.mapper.toEntity
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.db.AppDatabase
import com.lampung.baktimarsada.db.dao.EventDao
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val dao: EventDao,
    private val database: AppDatabase,
    private val remoteDataSource: AppRemoteDataSource
) : EventRepository {

    override fun observeEvents(): Flow<List<EventDetail>> {
        return dao.observeAll().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            val items = remoteDataSource.fetchEvents(sectorContext.sectorId).map { it.toEntity() }
            database.withTransaction {
                dao.clearAll()
                dao.insertAll(items)
            }
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to load events", throwable) }
        )
    }

    override suspend fun save(event: EventDetail, sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            remoteDataSource.saveEvent(event.toDto())
            refresh(sectorContext)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to save event", throwable) }
        )
    }

    override suspend fun delete(eventId: String, sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            remoteDataSource.deleteEvent(eventId)
            refresh(sectorContext)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to delete event", throwable) }
        )
    }
}
