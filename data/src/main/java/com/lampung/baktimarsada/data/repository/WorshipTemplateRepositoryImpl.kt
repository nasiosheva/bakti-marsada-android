package com.lampung.baktimarsada.data.repository

import androidx.room.withTransaction
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.data.mapper.toDomain
import com.lampung.baktimarsada.data.mapper.toDto
import com.lampung.baktimarsada.data.mapper.toEntity
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.db.AppDatabase
import com.lampung.baktimarsada.db.dao.WorshipTemplateDao
import com.lampung.baktimarsada.db.dao.WorshipTemplateItemDao
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.WorshipTemplate
import com.lampung.baktimarsada.domain.repository.WorshipTemplateRepository
import com.lampung.baktimarsada.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorshipTemplateRepositoryImpl @Inject constructor(
    private val templateDao: WorshipTemplateDao,
    private val templateItemDao: WorshipTemplateItemDao,
    private val database: AppDatabase,
    private val remoteDataSource: AppRemoteDataSource,
    private val authRepository: AuthRepository
) : WorshipTemplateRepository {

    private suspend fun currentTenantId(): String =
        authRepository.getCurrentSession()?.tenantContext?.tenantId.orEmpty()

    override fun observeTemplates(): Flow<List<WorshipTemplate>> {
        return combine(
            templateDao.observeAll(),
            templateItemDao.observeAll()
        ) { templates, items ->
            val itemsByTemplate = items.map { it.toDomain() }.groupBy { it.templateId }
            templates.map { template -> template.toDomain(itemsByTemplate[template.id].orEmpty()) }
        }
    }

    override suspend fun refresh(sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            val tenantId = currentTenantId()
            val remoteItems = remoteDataSource.fetchWorshipTemplates(tenantId, sectorContext.sectorId)
            val templates = remoteItems.map { it.toEntity() }
            val templateItems = remoteItems.flatMap { template ->
                template.items.map { it.toEntity(template.id) }
            }
            database.withTransaction {
                templateDao.clearAll()
                templateItemDao.clearAll()
                templateDao.insertAll(templates)
                templateItemDao.insertAll(templateItems)
            }
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to load worship templates", throwable) }
        )
    }

    override suspend fun save(template: WorshipTemplate, sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            val tenantId = currentTenantId()
            remoteDataSource.saveWorshipTemplate(tenantId, template.toDto())
            refresh(sectorContext)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to save worship template", throwable) }
        )
    }

    override suspend fun delete(templateId: String, sectorContext: SectorContext): AppResult<Unit> {
        return runCatching {
            val tenantId = currentTenantId()
            remoteDataSource.deleteWorshipTemplate(tenantId, templateId)
            refresh(sectorContext)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable -> AppResult.Error(throwable.message ?: "Failed to delete worship template", throwable) }
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
