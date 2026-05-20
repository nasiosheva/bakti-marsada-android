package com.lampung.baktimarsada.feature.onboarding.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.ui.component.BaktiDropdown
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import com.lampung.baktimarsada.ui.component.BaktiToolbar

@Composable
fun RegisterChurchRoute(
    onBack: () -> Unit,
    onRegistered: (UserRole) -> Unit,
    viewModel: RegisterChurchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.registeredRole) {
        val role = state.registeredRole ?: return@LaunchedEffect
        viewModel.consumeNavigation()
        onRegistered(role)
    }

    RegisterChurchScreen(
        state = state,
        onBack = onBack,
        onChurchNameChanged = viewModel::onChurchNameChanged,
        onDenominationChanged = viewModel::onDenominationChanged,
        onTerminologyChanged = viewModel::onTerminologyChanged,
        onAdminFullNameChanged = viewModel::onAdminFullNameChanged,
        onAdminEmailChanged = viewModel::onAdminEmailChanged,
        onAdminPasswordChanged = viewModel::onAdminPasswordChanged,
        onSubmit = viewModel::onSubmit
    )
}

@Composable
fun RegisterChurchScreen(
    state: RegisterChurchUiState,
    onBack: () -> Unit,
    onChurchNameChanged: (String) -> Unit,
    onDenominationChanged: (String) -> Unit,
    onTerminologyChanged: (String) -> Unit,
    onAdminFullNameChanged: (String) -> Unit,
    onAdminEmailChanged: (String) -> Unit,
    onAdminPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Scaffold(
        topBar = {
            BaktiToolbar(
                title = stringResource(id = R.string.register_church_title),
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RegisterChurchHeader()

                state.errorMessage?.let { BaktiSectionMessage(message = it) }

                FormSectionCard(title = stringResource(id = R.string.register_section_church)) {
                    BaktiTextInput(
                        value = state.churchName,
                        label = stringResource(id = R.string.register_field_church_name),
                        onValueChange = onChurchNameChanged,
                        isError = state.churchNameError != null,
                        errorMessage = state.churchNameError.orEmpty(),
                        modifier = Modifier.semantics { testTag = "register_church_name" }
                    )
                    BaktiDropdown(
                        selectedValue = state.denomination,
                        label = stringResource(id = R.string.register_field_denomination),
                        options = RegisterChurchUiState.DENOMINATION_OPTIONS,
                        onValueSelected = onDenominationChanged
                    )
                    BaktiDropdown(
                        selectedValue = state.terminologyPreset,
                        label = stringResource(id = R.string.register_field_terminology),
                        options = RegisterChurchUiState.TERMINOLOGY_OPTIONS,
                        onValueSelected = onTerminologyChanged
                    )
                }

                FormSectionCard(title = stringResource(id = R.string.register_section_admin)) {
                    BaktiTextInput(
                        value = state.adminFullName,
                        label = stringResource(id = R.string.register_field_admin_name),
                        onValueChange = onAdminFullNameChanged,
                        isError = state.adminFullNameError != null,
                        errorMessage = state.adminFullNameError.orEmpty(),
                        modifier = Modifier.semantics { testTag = "register_admin_name" }
                    )
                    BaktiTextInput(
                        value = state.adminEmail,
                        label = stringResource(id = R.string.register_field_admin_email),
                        onValueChange = onAdminEmailChanged,
                        isError = state.adminEmailError != null,
                        errorMessage = state.adminEmailError.orEmpty(),
                        modifier = Modifier.semantics { testTag = "register_admin_email" }
                    )
                    BaktiTextInput(
                        value = state.adminPassword,
                        label = stringResource(id = R.string.register_field_admin_password),
                        onValueChange = onAdminPasswordChanged,
                        isError = state.adminPasswordError != null,
                        errorMessage = state.adminPasswordError.orEmpty(),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.semantics { testTag = "register_admin_password" }
                    )
                }

                Spacer(modifier = Modifier.size(4.dp))

                Button(
                    onClick = onSubmit,
                    enabled = state.canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "register_submit_button" },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = stringResource(id = R.string.register_action_submit),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = onBack,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = stringResource(id = R.string.register_action_back_to_login))
                }
            }
        }
    }
}

@Composable
private fun RegisterChurchHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                    )
                )
            )
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.register_hero_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = stringResource(id = R.string.register_hero_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                    )
                }
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
