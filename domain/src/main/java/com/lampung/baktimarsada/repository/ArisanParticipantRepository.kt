package com.lampung.baktimarsada.repository

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.SectorContext

interface ArisanParticipantRepository {
    suspend fun fetch(sectorContext: SectorContext): AppResult<List<String>>
    suspend fun replace(sectorContext: SectorContext, memberIds: List<String>): AppResult<List<String>>
    suspend fun fillAllFromMembers(sectorContext: SectorContext): AppResult<List<String>>
}

// created by Mories Deo Hutapea, S.E.,S.Kom
