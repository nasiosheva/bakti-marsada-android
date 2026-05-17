package com.lampung.baktimarsada.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppBuildConfig
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.resources.StringProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.domain.usecase.LoginUseCase
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    onLoginSuccess: (UserRole) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedInRole) {
        state.loggedInRole?.let(onLoginSuccess)
    }

    LoginScreen(
        state = state,
        onIdentifierChanged = viewModel::onIdentifierChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onLoginClicked = viewModel::submit,
        onLoginAsAdminClicked = viewModel::submitWithDemoAdmin,
        onLoginAsJemaatClicked = viewModel::submitWithDemoJemaat,
        onResetAndLoginAsAdminClicked = viewModel::resetSimulationAndLoginAsAdmin
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onIdentifierChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClicked: () -> Unit,
    onLoginAsAdminClicked: () -> Unit = {},
    onLoginAsJemaatClicked: () -> Unit = {},
    onResetAndLoginAsAdminClicked: () -> Unit = {},
    isSimulationEnabled: Boolean = AppBuildConfig.simulationEnabled
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(id = R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium
            )
            if (isSimulationEnabled) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.login_simulate_badge),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BaktiTextInput(
                        value = state.identifier,
                        label = stringResource(id = R.string.login_identifier_label),
                        onValueChange = onIdentifierChanged,
                        modifier = Modifier.semantics { testTag = "login_identifier" },
                        isError = state.identifierError != null,
                        errorMessage = state.identifierError.orEmpty(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    BaktiTextInput(
                        value = state.password,
                        label = stringResource(id = R.string.login_password_label),
                        onValueChange = onPasswordChanged,
                        modifier = Modifier.semantics { testTag = "login_password" },
                        isError = state.passwordError != null,
                        errorMessage = state.passwordError.orEmpty(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    state.errorMessage?.let {
                        BaktiSectionMessage(message = it)
                    }
                    Button(
                        onClick = onLoginClicked,
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = "login_button" }
                    ) {
                        Text(
                            text = if (state.isLoading) {
                                stringResource(id = R.string.login_loading)
                            } else {
                                stringResource(id = R.string.login_button)
                            }
                        )
                    }
                }
            }
            if (isSimulationEnabled) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.login_demo_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(
                                id = R.string.login_demo_admin,
                                AppConstants.SAMPLE_ADMIN_IDENTIFIER,
                                AppConstants.SAMPLE_ADMIN_PASSWORD
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(
                                id = R.string.login_demo_jemaat,
                                AppConstants.SAMPLE_JEMAAT_IDENTIFIER,
                                AppConstants.SAMPLE_JEMAAT_PASSWORD
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(
                            onClick = onLoginAsAdminClicked,
                            enabled = !state.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { testTag = "login_demo_admin_button" }
                        ) {
                            Text(text = stringResource(id = R.string.login_demo_admin_button))
                        }
                        OutlinedButton(
                            onClick = onLoginAsJemaatClicked,
                            enabled = !state.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { testTag = "login_demo_jemaat_button" }
                        ) {
                            Text(text = stringResource(id = R.string.login_demo_jemaat_button))
                        }
                        OutlinedButton(
                            onClick = onResetAndLoginAsAdminClicked,
                            enabled = !state.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { testTag = "login_demo_reset_admin_button" }
                        ) {
                            Text(text = stringResource(id = R.string.login_demo_reset_admin_button))
                        }
                    }
                }
            }
        }
    }
}

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val identifierError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val loggedInRole: UserRole? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val dispatcherProvider: DispatcherProvider,
    private val stringProvider: StringProvider,
    private val remoteDataSource: AppRemoteDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onIdentifierChanged(value: String) {
        _state.update {
            it.copy(
                identifier = value,
                identifierError = null,
                errorMessage = null,
                loggedInRole = null
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _state.update {
            it.copy(
                password = value,
                passwordError = null,
                errorMessage = null,
                loggedInRole = null
            )
        }
    }

    fun submit() {
        val snapshot = _state.value
        submitCredentials(snapshot.identifier, snapshot.password)
    }

    fun submitWithDemoAdmin() {
        submitCredentials(
            identifier = AppConstants.SAMPLE_ADMIN_IDENTIFIER,
            password = AppConstants.SAMPLE_ADMIN_PASSWORD
        )
    }

    fun submitWithDemoJemaat() {
        submitCredentials(
            identifier = AppConstants.SAMPLE_JEMAAT_IDENTIFIER,
            password = AppConstants.SAMPLE_JEMAAT_PASSWORD
        )
    }

    fun resetSimulationAndLoginAsAdmin() {
        viewModelScope.launch(dispatcherProvider.io) {
            _state.update { it.copy(isLoading = true, errorMessage = null, loggedInRole = null) }
            runCatching {
                remoteDataSource.resetSimulationData()
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: stringProvider.get(R.string.login_error_simulation_reset_failed)
                    )
                }
                return@launch
            }
            loginWithCredentials(
                identifier = AppConstants.SAMPLE_ADMIN_IDENTIFIER,
                password = AppConstants.SAMPLE_ADMIN_PASSWORD
            )
        }
    }

    private fun submitCredentials(identifier: String, password: String) {
        var hasError = false
        if (identifier.isBlank()) {
            hasError = true
            _state.update { it.copy(identifierError = stringProvider.get(R.string.login_error_identifier_required)) }
        }
        if (password.isBlank()) {
            hasError = true
            _state.update { it.copy(passwordError = stringProvider.get(R.string.login_error_password_required)) }
        }
        if (hasError) return

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
            loginWithCredentials(normalizedIdentifier, password)
        }
    }

    private suspend fun loginWithCredentials(identifier: String, password: String) {
        _state.update { it.copy(isLoading = true, errorMessage = null, loggedInRole = null) }
        when (val result = loginUseCase(identifier, password)) {
            is AppResult.Success -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        loggedInRole = result.data.role
                    )
                }
            }
            is AppResult.Error -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
