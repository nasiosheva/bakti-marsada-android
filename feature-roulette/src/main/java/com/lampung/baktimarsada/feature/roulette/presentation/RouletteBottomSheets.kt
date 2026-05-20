package com.lampung.baktimarsada.feature.roulette.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.lampung.baktimarsada.feature.roulette.R
import com.lampung.baktimarsada.feature.roulette.model.RouletteUiState
import com.lampung.baktimarsada.feature.roulette.presentation.RouletteBottomSheet
import com.lampung.baktimarsada.feature.roulette.presentation.RouletteTextInput

@Composable
internal fun RouletteEditNamesSheet(
    visible: Boolean,
    editableNames: String,
    onEditableNamesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    if (!visible) return

    RouletteBottomSheet(
        onDismissRequest = onDismiss,
        title = stringResource(id = R.string.roulette_names_sheet_title)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(id = R.string.roulette_names_sheet_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            RouletteTextInput(
                value = editableNames,
                label = stringResource(id = R.string.roulette_names_sheet_label),
                onValueChange = onEditableNamesChange,
                singleLine = false,
                modifier = Modifier.heightIn(min = 180.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.roulette_action_cancel))
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.roulette_action_save_names))
                }
            }
        }
    }
}

@Composable
internal fun RouletteImportMembersSheet(
    visible: Boolean,
    state: RouletteUiState,
    loginIdentifier: String,
    loginPassword: String,
    selectedMemberIds: Set<String>,
    onLoginIdentifierChange: (String) -> Unit,
    onLoginPasswordChange: (String) -> Unit,
    onSelectedMemberIdsChange: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
    onLogin: () -> Unit,
    onRefreshMembers: () -> Unit,
    onApplyImport: () -> Unit
) {
    if (!visible) return

    RouletteBottomSheet(
        onDismissRequest = onDismiss,
        title = stringResource(id = R.string.roulette_import_sheet_title)
    ) {
        if (state.session == null) {
            RouletteImportLoginContent(
                loginIdentifier = loginIdentifier,
                loginPassword = loginPassword,
                isLoggingIn = state.isLoggingIn,
                onLoginIdentifierChange = onLoginIdentifierChange,
                onLoginPasswordChange = onLoginPasswordChange,
                onDismiss = onDismiss,
                onLogin = onLogin
            )
        } else {
            RouletteImportSelectionContent(
                state = state,
                selectedMemberIds = selectedMemberIds,
                onSelectedMemberIdsChange = onSelectedMemberIdsChange,
                onDismiss = onDismiss,
                onRefreshMembers = onRefreshMembers,
                onApplyImport = onApplyImport
            )
        }
    }
}

@Composable
private fun RouletteImportLoginContent(
    loginIdentifier: String,
    loginPassword: String,
    isLoggingIn: Boolean,
    onLoginIdentifierChange: (String) -> Unit,
    onLoginPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onLogin: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(id = R.string.roulette_import_login_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        RouletteTextInput(
            value = loginIdentifier,
            label = stringResource(id = R.string.roulette_import_identifier),
            onValueChange = onLoginIdentifierChange
        )
        RouletteTextInput(
            value = loginPassword,
            label = stringResource(id = R.string.roulette_import_password),
            onValueChange = onLoginPasswordChange,
            visualTransformation = PasswordVisualTransformation()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(id = R.string.roulette_action_cancel))
            }
            Button(
                onClick = onLogin,
                modifier = Modifier.weight(1f),
                enabled = !isLoggingIn
            ) {
                if (isLoggingIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = stringResource(id = R.string.roulette_action_login))
                }
            }
        }
    }
}

@Composable
private fun RouletteImportSelectionContent(
    state: RouletteUiState,
    selectedMemberIds: Set<String>,
    onSelectedMemberIdsChange: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
    onRefreshMembers: () -> Unit,
    onApplyImport: () -> Unit
) {
    val session = state.session ?: return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ImportSessionCard(
            session = session,
            isLoading = state.isLoadingMembers,
            onRefresh = onRefreshMembers
        )
        Text(
            text = stringResource(id = R.string.roulette_import_selection_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (state.members.isEmpty() && !state.isLoadingMembers) {
            EmptyMembersCard()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.members, key = { it.id }) { member ->
                    MemberSelectionRow(
                        member = member,
                        selected = member.id in selectedMemberIds,
                        onToggle = {
                            onSelectedMemberIdsChange(
                                selectedMemberIds.toMutableSet().apply {
                                    if (!add(member.id)) remove(member.id)
                                }.toSet()
                            )
                        }
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(id = R.string.roulette_action_cancel))
            }
            Button(
                onClick = onApplyImport,
                modifier = Modifier.weight(1f),
                enabled = state.members.isNotEmpty()
            ) {
                Text(text = stringResource(id = R.string.roulette_action_apply_import))
            }
        }
    }
}

@Composable
internal fun RouletteWinnerSheet(
    winnerName: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val safeWinnerName = winnerName ?: return
    RouletteBottomSheet(
        onDismissRequest = onDismiss,
        title = stringResource(id = R.string.roulette_winner_title)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Text(
                text = safeWinnerName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.roulette_winner_message, safeWinnerName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { onConfirm(safeWinnerName) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.roulette_action_close))
            }
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
