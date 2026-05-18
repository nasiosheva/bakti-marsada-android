package com.lampung.baktimarsada.domain.usecase

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.repository.AuthRepository
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): AppResult<SessionState> {
        return repository.loginWithGoogle(idToken)
    }
}
