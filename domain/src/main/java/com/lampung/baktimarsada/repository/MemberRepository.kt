package com.lampung.baktimarsada.domain.repository

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.SectorContext
import kotlinx.coroutines.flow.Flow

interface MemberRepository {
    fun observeMembers(): Flow<List<MemberDetail>>
    suspend fun refresh(sectorContext: SectorContext): AppResult<Unit>
    suspend fun save(member: MemberDetail, sectorContext: SectorContext): AppResult<Unit>
    suspend fun delete(memberId: String, sectorContext: SectorContext): AppResult<Unit>
}

// created by Mories Deo Hutapea, S.E.,S.Kom
