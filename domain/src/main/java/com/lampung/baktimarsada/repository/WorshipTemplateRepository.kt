package com.lampung.baktimarsada.domain.repository

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.WorshipTemplate
import kotlinx.coroutines.flow.Flow

interface WorshipTemplateRepository {
    fun observeTemplates(): Flow<List<WorshipTemplate>>
    suspend fun refresh(sectorContext: SectorContext): AppResult<Unit>
    suspend fun save(template: WorshipTemplate, sectorContext: SectorContext): AppResult<Unit>
    suspend fun delete(templateId: String, sectorContext: SectorContext): AppResult<Unit>
}

// created by Mories Deo Hutapea, S.E.,S.Kom
