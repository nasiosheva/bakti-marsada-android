package com.lampung.baktimarsada.feature.auth.presentation

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
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
import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.domain.usecase.LoginUseCase
import com.lampung.baktimarsada.domain.usecase.LoginWithGoogleUseCase
import com.lampung.baktimarsada.firebase.core.auth.GoogleSignInHelper
import com.lampung.baktimarsada.firebase.core.auth.GoogleSignInToken
import com.lampung.baktimarsada.firebase.notification.NotificationHelper
import com.lampung.baktimarsada.security.SecureStorage
import com.lampung.baktimarsada.ui.component.BaktiCheckbox
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme
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
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            viewModel.onGoogleSignInCancelled()
            return@rememberLauncherForActivityResult
        }
        viewModel.onGoogleSignInResult(result.data)
    }

    LaunchedEffect(state.loggedInRole) {
        state.loggedInRole?.let(onLoginSuccess)
    }

    LoginScreen(
        state = state,
        onIdentifierChanged = viewModel::onIdentifierChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onRememberMeChanged = viewModel::onRememberMeChanged,
        onLoginClicked = viewModel::submit,
        onGoogleSignInClicked = {
            viewModel.createGoogleSignInIntent()?.let(googleSignInLauncher::launch)
                ?: viewModel.onGoogleSignInUnavailable()
        },
        onLoginAsAdminClicked = viewModel::submitWithDemoAdmin,
        onLoginAsJemaatClicked = viewModel::submitWithDemoJemaat,
        onResetAndLoginAsAdminClicked = viewModel::resetSimulationAndLoginAsAdmin,
        isGoogleSignInEnabled = state.isGoogleSignInEnabled
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onIdentifierChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRememberMeChanged: (Boolean) -> Unit = {},
    onLoginClicked: () -> Unit,
    onGoogleSignInClicked: () -> Unit = {},
    onLoginAsAdminClicked: () -> Unit = {},
    onLoginAsJemaatClicked: () -> Unit = {},
    onResetAndLoginAsAdminClicked: () -> Unit = {},
    isGoogleSignInEnabled: Boolean = false,
    isSimulationEnabled: Boolean = AppBuildConfig.simulationEnabled
) {
    val tenant = TenantRuntime.current
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.22f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(20.dp)
                    .widthIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoginHeader(
                    title = tenant.appDisplayName,
                    subtitle = stringResource(id = R.string.login_subtitle),
                    isSimulationEnabled = isSimulationEnabled
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
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
                            visualTransformation = if (isPasswordVisible) {
                                androidx.compose.ui.text.input.VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isPasswordVisible = !isPasswordVisible }
                                ) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = if (isPasswordVisible) {
                                            stringResource(id = R.string.login_password_hide)
                                        } else {
                                            stringResource(id = R.string.login_password_show)
                                        }
                                    )
                                }
                            }
                        )
                        BaktiCheckbox(
                            checked = state.rememberMe,
                            label = stringResource(id = R.string.login_remember_me),
                            onCheckedChange = onRememberMeChanged
                        )
                        state.errorMessage?.let {
                            BaktiSectionMessage(message = it)
                        }
                        Button(
                            onClick = onLoginClicked,
                            enabled = !state.isLoading && state.password.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
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
                        OutlinedButton(
                            onClick = onGoogleSignInClicked,
                            enabled = !state.isLoading && isGoogleSignInEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .semantics { testTag = "login_google_button" }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.login_google_badge),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(text = stringResource(id = R.string.login_google_button))
                            }
                        }
                    }
                }
                if (isSimulationEnabled) {
                    DemoLoginCard(
                        adminIdentifier = tenant.sampleAdminIdentifier,
                        adminPassword = tenant.sampleAdminPassword,
                        jemaatIdentifier = tenant.sampleJemaatIdentifier,
                        jemaatPassword = tenant.sampleJemaatPassword,
                        isLoading = state.isLoading,
                        onLoginAsAdminClicked = onLoginAsAdminClicked,
                        onLoginAsJemaatClicked = onLoginAsJemaatClicked,
                        onResetAndLoginAsAdminClicked = onResetAndLoginAsAdminClicked
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginHeader(
    title: String,
    subtitle: String,
    isSimulationEnabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = title.toLoginInitials(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSimulationEnabled) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Text(
                    text = stringResource(id = R.string.login_simulate_badge),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun DemoLoginCard(
    adminIdentifier: String,
    adminPassword: String,
    jemaatIdentifier: String,
    jemaatPassword: String,
    isLoading: Boolean,
    onLoginAsAdminClicked: () -> Unit,
    onLoginAsJemaatClicked: () -> Unit,
    onResetAndLoginAsAdminClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.login_demo_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(
                        id = R.string.login_demo_admin,
                        adminIdentifier,
                        adminPassword
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(
                        id = R.string.login_demo_jemaat,
                        jemaatIdentifier,
                        jemaatPassword
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onLoginAsAdminClicked,
                    enabled = !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { testTag = "login_demo_admin_button" }
                ) {
                    Text(text = stringResource(id = R.string.profile_role_admin))
                }
                OutlinedButton(
                    onClick = onLoginAsJemaatClicked,
                    enabled = !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { testTag = "login_demo_jemaat_button" }
                ) {
                    Text(text = stringResource(id = R.string.profile_role_jemaat))
                }
            }
            OutlinedButton(
                onClick = onResetAndLoginAsAdminClicked,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "login_demo_reset_admin_button" }
            ) {
                Text(text = stringResource(id = R.string.login_demo_reset_admin_button))
            }
        }
    }
}

private fun String.toLoginInitials(): String {
    return split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString(separator = "") { it.first().uppercaseChar().toString() }
        .ifBlank { "BM" }
}

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

    fun onRememberMeChanged(value: Boolean) {
        _state.update { it.copy(rememberMe = value) }
    }

    fun submit() {
        val snapshot = _state.value
        submitCredentials(snapshot.identifier, snapshot.password)
    }

    fun createGoogleSignInIntent(): Intent? = googleSignInHelper.createSignInIntent()

    fun onGoogleSignInUnavailable() {
        _state.update {
            it.copy(
                errorMessage = stringProvider.get(R.string.login_google_unavailable),
                loggedInRole = null
            )
        }
    }

    fun onGoogleSignInCancelled() {
        _state.update { it.copy(isLoading = false) }
    }

    fun onGoogleSignInResult(data: Intent?) {
        viewModelScope.launch(dispatcherProvider.io) {
            _state.update { it.copy(isLoading = true, errorMessage = null, loggedInRole = null) }
            val tokenResult = googleSignInHelper.extractToken(data)
            tokenResult.fold(
                onSuccess = { token ->
                    loginWithGoogleToken(token)
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message
                                ?: stringProvider.get(R.string.login_google_failed)
                        )
                    }
                }
            )
        }
    }

    fun submitWithDemoAdmin() {
        submitCredentials(
            identifier = tenant.sampleAdminIdentifier,
            password = tenant.sampleAdminPassword
        )
    }

    fun submitWithDemoJemaat() {
        submitCredentials(
            identifier = tenant.sampleJemaatIdentifier,
            password = tenant.sampleJemaatPassword
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
                identifier = tenant.sampleAdminIdentifier,
                password = tenant.sampleAdminPassword
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
                persistRememberedCredentials(identifier, password, _state.value.rememberMe)
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

    private suspend fun loginWithGoogleToken(token: GoogleSignInToken) {
        when (val result = loginWithGoogleUseCase(token.idToken)) {
            is AppResult.Success -> {
                persistRememberedCredentials(
                    rememberMe = false,
                    identifier = "",
                    password = ""
                )
                notificationHelper.showGoogleWelcomeNotification(
                    accountLabel = token.email
                        ?.takeIf { it.isNotBlank() }
                        ?: result.data.displayName.takeIf { it.isNotBlank() }
                        ?: result.data.userId
                )
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
                        errorMessage = result.message,
                        loggedInRole = null
                    )
                }
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
}

// created by Mories Deo Hutapea, S.E.,S.Kom

@Preview(name = "Login - default", showBackground = true)
@Composable
private fun LoginScreenDefaultPreview() {
    BaktiMarsadaTheme {
        LoginScreen(
            state = LoginUiState(
                identifier = "admin@demo.com",
                password = "password123",
                isGoogleSignInEnabled = true
            ),
            onIdentifierChanged = {},
            onPasswordChanged = {},
            onLoginClicked = {},
            onGoogleSignInClicked = {},
            onLoginAsAdminClicked = {},
            onLoginAsJemaatClicked = {},
            onResetAndLoginAsAdminClicked = {},
            isSimulationEnabled = true
        )
    }
}

@Preview(name = "Login - validation error", showBackground = true)
@Composable
private fun LoginScreenValidationErrorPreview() {
    BaktiMarsadaTheme {
        LoginScreen(
            state = LoginUiState(
                identifier = "",
                password = "bad",
                identifierError = "Identifier wajib diisi",
                passwordError = "Password wajib diisi",
                errorMessage = "Login gagal",
                isGoogleSignInEnabled = true
            ),
            onIdentifierChanged = {},
            onPasswordChanged = {},
            onLoginClicked = {},
            onGoogleSignInClicked = {},
            onLoginAsAdminClicked = {},
            onLoginAsJemaatClicked = {},
            onResetAndLoginAsAdminClicked = {},
            isSimulationEnabled = false
        )
    }
}
