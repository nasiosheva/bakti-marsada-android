package com.lampung.baktimarsada.domain.usecase

import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.repository.AuthRepository
import javax.inject.Inject

class BootstrapSessionUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): SessionState? = repository.bootstrapSession()
}

// created by Mories Deo Hutapea, S.E.,S.Kom
