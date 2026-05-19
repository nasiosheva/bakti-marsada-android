package com.lampung.baktimarsada.feature.roulette.presentation

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lampung.baktimarsada.feature.roulette.R
import com.lampung.baktimarsada.feature.roulette.presentation.RouletteResponseSnackbarEffect
import com.lampung.baktimarsada.feature.roulette.presentation.RouletteSnackbarHost
import com.lampung.baktimarsada.feature.roulette.presentation.RouletteToolbar

@Composable
fun RouletteApp(
    initialNames: List<String> = emptyList(),
    onBack: (() -> Unit)? = null,
    onWinnerConfirmed: ((String) -> Unit)? = null,
    viewModel: RouletteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showEditSheet by rememberSaveable { mutableStateOf(false) }
    var showImportSheet by rememberSaveable { mutableStateOf(false) }
    var editableNames by rememberSaveable { mutableStateOf("") }
    var loginIdentifier by rememberSaveable { mutableStateOf("") }
    var loginPassword by rememberSaveable { mutableStateOf("") }
    var selectedMemberIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(showEditSheet) {
        if (showEditSheet) {
            editableNames = state.names.joinToString(separator = "\n")
        }
    }

    LaunchedEffect(initialNames) {
        if (initialNames.isNotEmpty()) {
            viewModel.consumeIntentNames(initialNames)
        }
    }

    LaunchedEffect(showImportSheet) {
        if (showImportSheet) {
            viewModel.prepareImportMembers()
        }
    }

    LaunchedEffect(showImportSheet, state.session?.userId, state.members) {
        if (showImportSheet && state.session != null && state.members.isNotEmpty()) {
            selectedMemberIds = state.members.map { it.id }.toSet()
        }
    }

    RouletteResponseSnackbarEffect(
        message = state.message,
        hostState = snackbarHostState,
        onMessageConsumed = viewModel::consumeMessage
    )

    Scaffold(
        topBar = {
            RouletteToolbar(
                title = stringResource(id = R.string.roulette_title),
                subtitle = stringResource(id = R.string.roulette_subtitle),
                onBack = onBack
            )
        },
        snackbarHost = {
            RouletteSnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        RouletteHomeContent(
            state = state,
            innerPadding = innerPadding,
            onSpinRequested = viewModel::requestSpin,
            onSpinAnimationCompleted = viewModel::confirmSpin,
            onEditNames = { showEditSheet = true },
            onImportMembers = { showImportSheet = true },
            onClearHistory = viewModel::clearHistory
        )
    }

    RouletteEditNamesSheet(
        visible = showEditSheet,
        editableNames = editableNames,
        onEditableNamesChange = { editableNames = it },
        onDismiss = { showEditSheet = false },
        onSave = {
            viewModel.saveManualNames(editableNames)
            showEditSheet = false
        }
    )

    RouletteImportMembersSheet(
        visible = showImportSheet,
        state = state,
        loginIdentifier = loginIdentifier,
        loginPassword = loginPassword,
        selectedMemberIds = selectedMemberIds,
        onLoginIdentifierChange = { loginIdentifier = it },
        onLoginPasswordChange = { loginPassword = it },
        onSelectedMemberIdsChange = { selectedMemberIds = it },
        onDismiss = { showImportSheet = false },
        onLogin = {
            viewModel.loginForImport(
                identifier = loginIdentifier,
                password = loginPassword
            )
        },
        onRefreshMembers = viewModel::prepareImportMembers,
        onLogout = {
            viewModel.logoutImportSession()
            loginIdentifier = ""
            loginPassword = ""
            selectedMemberIds = emptySet()
        },
        onApplyImport = {
            viewModel.importSelectedMembers(selectedMemberIds)
            showImportSheet = false
        }
    )

    RouletteWinnerSheet(
        winnerName = state.latestWinner?.winnerName,
        onDismiss = viewModel::dismissWinner,
        onConfirm = { winnerName ->
            onWinnerConfirmed?.invoke(winnerName)
            viewModel.dismissWinner()
        }
    )
}

// created by Mories Deo Hutapea, S.E.,S.Kom
