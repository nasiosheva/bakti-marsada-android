package com.lampung.baktimarsada.feature.auth.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

@Preview(name = "Login - default")
@Composable
private fun LoginScreenPreview() {
    BaktiMarsadaTheme {
        LoginScreen(
            state = LoginUiState(
                identifier = "admin",
                password = "********",
                loggedInRole = UserRole.ADMIN
            ),
            onIdentifierChanged = {},
            onPasswordChanged = {},
            onLoginClicked = {},
            onLoginAsAdminClicked = {},
            onLoginAsJemaatClicked = {},
            onResetAndLoginAsAdminClicked = {}
        )
    }
}

@Preview(name = "Login - validation error")
@Composable
private fun LoginScreenErrorPreview() {
    BaktiMarsadaTheme {
        LoginScreen(
            state = LoginUiState(
                identifier = "",
                password = "bad",
                identifierError = "Identifier wajib diisi",
                passwordError = "Password terlalu pendek",
                errorMessage = "Login failed"
            ),
            onIdentifierChanged = {},
            onPasswordChanged = {},
            onLoginClicked = {},
            onLoginAsAdminClicked = {},
            onLoginAsJemaatClicked = {},
            onResetAndLoginAsAdminClicked = {}
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
