package com.lampung.baktimarsada.data.repository

import androidx.room.withTransaction
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.data.mapper.toDomain
import com.lampung.baktimarsada.data.mapper.toDto
import com.lampung.baktimarsada.data.mapper.toEntity
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.db.AppDatabase
import com.lampung.baktimarsada.db.dao.MemberDao
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemberRepositoryImpl @Inject constructor(
    private val dao: MemberDao,
    private val database: AppDatabase,
    private val remoteDataSource: AppRemoteDataSource
) : MemberRepository {

    override fun observeMembers(): Flow<List<MemberDetail>> {
        return dao.observeAll().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            val items = remoteDataSource.fetchMembers(sectorContext.sectorId).map { it.toEntity() }
            database.withTransaction {
                dao.clearAll()
                dao.insertAll(items)
            }
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to load members", throwable) }
        )
    }

    override suspend fun save(member: MemberDetail, sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            remoteDataSource.saveMember(member.toDto())
            refresh(sectorContext)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to save member", throwable) }
        )
    }

    override suspend fun delete(memberId: String, sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            remoteDataSource.deleteMember(memberId)
            refresh(sectorContext)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to delete member", throwable) }
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
