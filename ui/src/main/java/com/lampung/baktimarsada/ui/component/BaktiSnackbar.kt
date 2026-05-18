package com.lampung.baktimarsada.ui.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
fun BaktiSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    )
}

@Composable
fun BaktiResponseSnackbarEffect(
    message: String?,
    hostState: SnackbarHostState,
    onMessageConsumed: () -> Unit,
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    LaunchedEffect(message) {
        val value = message ?: return@LaunchedEffect
        hostState.showSnackbar(
            message = value,
            duration = duration
        )
        onMessageConsumed()
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
