package com.lampung.baktimarsada.feature.auth.presentation

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.resources.StringProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.domain.usecase.LoginUseCase
import com.lampung.baktimarsada.domain.usecase.LoginWithGoogleUseCase
import com.lampung.baktimarsada.firebase.core.auth.GoogleSignInHelper
import com.lampung.baktimarsada.firebase.core.auth.GoogleSignInToken
import com.lampung.baktimarsada.firebase.notification.NotificationHelper
import com.lampung.baktimarsada.security.SecureStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val dispatcherProvider: DispatcherProvider,
    private val stringProvider: StringProvider,
    private val remoteDataSource: AppRemoteDataSource,
    private val secureStorage: SecureStorage,
    private val googleSignInHelper: GoogleSignInHelper,
    private val notificationHelper: NotificationHelper
) : ViewModel() {
    private val tenant
        get() = TenantRuntime.current

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    init {
        restoreRememberedCredentials()
    }

    fun onIdentifierChanged(value: String) {
        updateStateForInput { copy(identifier = value, identifierError = null) }
    }

    fun onPasswordChanged(value: String) {
        updateStateForInput { copy(password = value, passwordError = null) }
    }

    fun onRememberMeChanged(value: Boolean) {
        _state.update { it.copy(rememberMe = value) }
    }

    fun clearErrorMessage() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun submit() {
        val snapshot = _state.value
        submitCredentials(snapshot.identifier, snapshot.password)
    }

    fun createGoogleSignInIntent(): Intent? = googleSignInHelper.createSignInIntent()

    fun onGoogleSignInUnavailable() {
        updateStateError(stringProvider.get(R.string.login_google_unavailable))
    }

    fun onGoogleSignInCancelled() {
        updateStateError(
            stringProvider.get(R.string.login_google_failed),
            isLoading = false
        )
    }

    fun onGoogleSignInResult(data: Intent?) {
        viewModelScope.launch(dispatcherProvider.io) {
            setLoading()
            val tokenResult = googleSignInHelper.extractToken(data)
            val token = tokenResult.getOrElse { throwable ->
                updateStateError(
                    throwable.message ?: stringProvider.get(R.string.login_google_failed),
                    isLoading = false
                )
                return@launch
            }
            performGoogleLogin(token)
        }
    }

    fun submitWithDemoAdmin() {
        submitCredentials(tenant.sampleAdminIdentifier, tenant.sampleAdminPassword)
    }

    fun submitWithDemoJemaat() {
        submitCredentials(tenant.sampleJemaatIdentifier, tenant.sampleJemaatPassword)
    }

    fun resetSimulationAndLoginAsAdmin() {
        viewModelScope.launch(dispatcherProvider.io) {
            setLoading()
            runCatching { remoteDataSource.resetSimulationData() }
                .onFailure { throwable ->
                    updateStateError(
                        throwable.message
                            ?: stringProvider.get(R.string.login_error_simulation_reset_failed),
                        isLoading = false
                    )
                    return@launch
                }
            performCredentialLogin(tenant.sampleAdminIdentifier, tenant.sampleAdminPassword)
        }
    }

    private fun restoreRememberedCredentials() {
        val rememberLogin = secureStorage.getString(AppConstants.KEY_REMEMBER_LOGIN) == "true"
        val rememberedIdentifier = secureStorage.getString(AppConstants.KEY_REMEMBER_IDENTIFIER).orEmpty()
        val rememberedPassword = secureStorage.getString(AppConstants.KEY_REMEMBER_PASSWORD).orEmpty()
        _state.update {
            it.copy(
                rememberMe = rememberLogin,
                identifier = if (rememberLogin) rememberedIdentifier else "",
                password = if (rememberLogin) rememberedPassword else "",
                isGoogleSignInEnabled = googleSignInHelper.isAvailable()
            )
        }
    }

    private fun submitCredentials(identifier: String, password: String) {
        val validationState = validateCredentials(identifier, password)
        if (validationState != null) {
            _state.update { validationState }
            return
        }

        val normalizedIdentifier = identifier.trim()
        _state.update {
            it.copy(
                identifier = normalizedIdentifier,
                password = password,
                identifierError = null,
                passwordError = null,
                errorMessage = null,
                loggedInRole = null
            )
        }
        viewModelScope.launch(dispatcherProvider.io) {
            performCredentialLogin(normalizedIdentifier, password)
        }
    }

    private fun validateCredentials(identifier: String, password: String): LoginUiState? {
        var identifierError: String? = null
        var passwordError: String? = null
        if (identifier.isBlank()) {
            identifierError = stringProvider.get(R.string.login_error_identifier_required)
        }
        if (password.isBlank()) {
            passwordError = stringProvider.get(R.string.login_error_password_required)
        }
        if (identifierError == null && passwordError == null) return null
        return _state.value.copy(
            identifierError = identifierError,
            passwordError = passwordError,
            errorMessage = null,
            loggedInRole = null
        )
    }

    private suspend fun performCredentialLogin(identifier: String, password: String) {
        setLoading()
        when (val result = loginUseCase(identifier, password)) {
            is AppResult.Success -> {
                persistRememberedCredentials(identifier, password, _state.value.rememberMe)
                updateStateSuccess(result.data.role)
            }
            is AppResult.Error -> {
                updateStateError(result.message, isLoading = false)
            }
        }
    }

    private suspend fun performGoogleLogin(token: GoogleSignInToken) {
        when (val result = loginWithGoogleUseCase(token.idToken)) {
            is AppResult.Success -> {
                persistRememberedCredentials("", "", rememberMe = false)
                notificationHelper.showGoogleWelcomeNotification(
                    accountLabel = token.email
                        ?.takeIf { it.isNotBlank() }
                        ?: result.data.displayName.takeIf { it.isNotBlank() }
                        ?: result.data.userId
                )
                updateStateSuccess(result.data.role)
            }
            is AppResult.Error -> {
                updateStateError(result.message, isLoading = false)
            }
        }
    }

    private fun persistRememberedCredentials(identifier: String, password: String, rememberMe: Boolean) {
        if (rememberMe) {
            secureStorage.putString(AppConstants.KEY_REMEMBER_LOGIN, "true")
            secureStorage.putString(AppConstants.KEY_REMEMBER_IDENTIFIER, identifier)
            secureStorage.putString(AppConstants.KEY_REMEMBER_PASSWORD, password)
        } else {
            secureStorage.putString(AppConstants.KEY_REMEMBER_LOGIN, "false")
            secureStorage.remove(AppConstants.KEY_REMEMBER_IDENTIFIER)
            secureStorage.remove(AppConstants.KEY_REMEMBER_PASSWORD)
        }
    }

    private fun setLoading() {
        _state.update { it.copy(isLoading = true, errorMessage = null, loggedInRole = null) }
    }

    private fun updateStateSuccess(role: UserRole) {
        _state.update {
            it.copy(
                isLoading = false,
                errorMessage = null,
                loggedInRole = role
            )
        }
    }

    private fun updateStateError(message: String, isLoading: Boolean = false) {
        _state.update {
            it.copy(
                isLoading = isLoading,
                errorMessage = extractErrorMessage(message),
                loggedInRole = null
            )
        }
    }

    private fun extractErrorMessage(rawMessage: String): String {
        val trimmed = rawMessage.trim()
        if (!trimmed.startsWith("{")) return rawMessage
        return runCatching {
            JSONObject(trimmed).optString("message").takeIf { it.isNotBlank() }
        }.getOrNull() ?: rawMessage
    }

    private fun updateStateForInput(transform: LoginUiState.() -> LoginUiState) {
        _state.update {
            it.transform().copy(
                errorMessage = null,
                loggedInRole = null
            )
        }
    }
}
