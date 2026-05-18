package com.lampung.baktimarsada.domain.usecase

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(identifier: String, password: String): AppResult<SessionState> {
        return repository.login(identifier, password)
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
