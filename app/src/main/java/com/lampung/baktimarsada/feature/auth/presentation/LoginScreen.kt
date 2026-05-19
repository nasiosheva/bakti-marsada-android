package com.lampung.baktimarsada.feature.auth.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppBuildConfig
import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.ui.component.BaktiCheckbox
import com.lampung.baktimarsada.ui.component.BaktiResponseSnackbarEffect
import com.lampung.baktimarsada.ui.component.BaktiSnackbarHost
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

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
        onErrorMessageConsumed = viewModel::clearErrorMessage,
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
    onErrorMessageConsumed: () -> Unit = {},
    isGoogleSignInEnabled: Boolean = false,
    isSimulationEnabled: Boolean = AppBuildConfig.simulationEnabled
) {
    val tenant = TenantRuntime.current
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    BaktiResponseSnackbarEffect(
        message = state.errorMessage,
        hostState = snackbarHostState,
        onMessageConsumed = onErrorMessageConsumed
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { BaktiSnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LoginBackgroundBrush())
        ) {
            LoginBackgroundPanels()
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .widthIn(max = 440.dp)
                    .heightIn(min = 0.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoginHeader(
                    title = tenant.appDisplayName,
                    subtitle = stringResource(id = R.string.login_subtitle),
                    isSimulationEnabled = isSimulationEnabled
                )
                LoginFormCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        LoginPrimaryButton(
                            text = if (state.isLoading) {
                                stringResource(id = R.string.login_loading)
                            } else {
                                stringResource(id = R.string.login_button)
                            },
                            enabled = !state.isLoading && state.password.isNotBlank(),
                            onClick = onLoginClicked
                        )
                        GoogleSignInButton(
                            enabled = !state.isLoading,
                            onClick = onGoogleSignInClicked
                        )
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
private fun LoginBackgroundBrush(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.36f)
        )
    )
}

@Composable
private fun BoxScope.LoginBackgroundPanels() {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(188.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.24f),
                        secondary.copy(alpha = 0.14f),
                        Color.Transparent
                    )
                )
            )
            .align(Alignment.TopCenter)
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(118.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.30f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.24f)
                    )
                )
            )
    )
}

@Composable
private fun LoginHeader(
    title: String,
    subtitle: String,
    isSimulationEnabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(88.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f),
                    shape = CircleShape
                ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.96f),
            contentColor = MaterialTheme.colorScheme.onPrimary,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_bakti_marsada_new),
                contentDescription = stringResource(id = R.string.app_name),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 360.dp)
            )
        }
        if (isSimulationEnabled) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.86f),
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.28f)
                )
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
private fun LoginFormCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .align(Alignment.TopCenter)
            )
            Box(modifier = Modifier.padding(top = 5.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun LoginPrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .semantics { testTag = "login_button" }
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun GoogleSignInButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = if (enabled) 0.58f else 0.24f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .semantics { testTag = "login_google_button" }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(id = R.string.login_google_badge),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(id = R.string.login_google_button),
                fontWeight = FontWeight.Medium
            )
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .semantics { testTag = "login_demo_admin_button" }
                ) {
                    Text(text = stringResource(id = R.string.profile_role_admin))
                }
                OutlinedButton(
                    onClick = onLoginAsJemaatClicked,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
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
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "login_demo_reset_admin_button" }
            ) {
                Text(text = stringResource(id = R.string.login_demo_reset_admin_button))
            }
        }
    }
}

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
