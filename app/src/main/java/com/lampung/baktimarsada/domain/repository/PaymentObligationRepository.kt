package com.lampung.baktimarsada.domain.repository

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.domain.model.SectorContext
import kotlinx.coroutines.flow.Flow

interface PaymentObligationRepository {
    fun observeObligations(): Flow<List<PaymentObligationDetail>>
    suspend fun refresh(sectorContext: SectorContext): AppResult<Unit>
    suspend fun save(obligation: PaymentObligationDetail, sectorContext: SectorContext): AppResult<Unit>
    suspend fun delete(obligationId: String, sectorContext: SectorContext): AppResult<Unit>
}
