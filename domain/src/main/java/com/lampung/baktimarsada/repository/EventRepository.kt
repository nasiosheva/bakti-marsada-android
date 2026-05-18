package com.lampung.baktimarsada.repository

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.SectorContext
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun observeEvents(): Flow<List<EventDetail>>
    suspend fun refresh(sectorContext: SectorContext): AppResult<Unit>
    suspend fun save(event: EventDetail, sectorContext: SectorContext): AppResult<Unit>
    suspend fun delete(eventId: String, sectorContext: SectorContext): AppResult<Unit>
}

// created by Mories Deo Hutapea, S.E.,S.Kom
