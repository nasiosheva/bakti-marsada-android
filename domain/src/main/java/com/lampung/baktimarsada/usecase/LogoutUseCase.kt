package com.lampung.baktimarsada.domain.usecase

import com.lampung.baktimarsada.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() = repository.logout()
}

// created by Mories Deo Hutapea, S.E.,S.Kom
