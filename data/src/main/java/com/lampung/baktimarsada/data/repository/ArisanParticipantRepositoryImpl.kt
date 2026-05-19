package com.lampung.baktimarsada.data.repository

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.repository.ArisanParticipantRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArisanParticipantRepositoryImpl @Inject constructor(
    private val remoteDataSource: AppRemoteDataSource
) : ArisanParticipantRepository {

    override suspend fun fetch(sectorContext: SectorContext): AppResult<List<String>> {
        return runCatching {
            remoteDataSource.fetchArisanParticipants(sectorContext.sectorId)
                .map { it.memberId }
                .distinct()
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { throwable ->
                AppResult.Error(throwable.message ?: "Failed to load arisan participants", throwable)
            }
        )
    }

    override suspend fun replace(sectorContext: SectorContext, memberIds: List<String>): AppResult<List<String>> {
        return runCatching {
            remoteDataSource.replaceArisanParticipants(
                sectorId = sectorContext.sectorId,
                memberIds = memberIds
            ).map { it.memberId }.distinct()
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { throwable ->
                AppResult.Error(throwable.message ?: "Failed to update arisan participants", throwable)
            }
        )
    }

    override suspend fun fillAllFromMembers(sectorContext: SectorContext): AppResult<List<String>> {
        return runCatching {
            remoteDataSource.fillArisanParticipantsFromMembers(sectorContext.sectorId)
                .map { it.memberId }
                .distinct()
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { throwable ->
                AppResult.Error(throwable.message ?: "Failed to fill arisan participants", throwable)
            }
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
