package com.lampung.baktimarsada.domain.usecase

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.repository.AuthRepository
import javax.inject.Inject

class SyncFcmTokenUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(token: String): AppResult<Unit> = repository.syncFcmToken(token)
}

// created by Mories Deo Hutapea, S.E.,S.Kom
