package com.lampung.baktimarsada.domain.usecase

import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSessionUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<SessionState?> = repository.observeSession()
}

// created by Mories Deo Hutapea, S.E.,S.Kom
