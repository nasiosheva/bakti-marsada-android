package com.lampung.baktimarsada.feature.auth.presentation

import com.lampung.baktimarsada.domain.model.UserRole

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val identifierError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isGoogleSignInEnabled: Boolean = false,
    val loggedInRole: UserRole? = null
)
