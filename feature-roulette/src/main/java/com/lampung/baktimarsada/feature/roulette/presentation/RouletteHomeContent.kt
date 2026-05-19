package com.lampung.baktimarsada.feature.roulette.presentation

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.feature.roulette.R
import com.lampung.baktimarsada.feature.roulette.model.RouletteHistoryItem
import com.lampung.baktimarsada.feature.roulette.model.RouletteNameSource
import com.lampung.baktimarsada.feature.roulette.model.RoulettePendingSpin
import com.lampung.baktimarsada.feature.roulette.model.RouletteUiState
import com.lampung.baktimarsada.ui.component.JemaatPill
import java.text.SimpleDateFormat
import java.util.Date
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun RouletteHomeContent(
    state: RouletteUiState,
    innerPadding: PaddingValues,
    onSpinRequested: (Float) -> Boolean,
    onSpinAnimationCompleted: (Long) -> Unit,
    onEditNames: () -> Unit,
    onImportMembers: () -> Unit,
    onClearHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { RouletteSummaryCard(state = state) }
        item {
            RouletteWheelCard(
                names = state.names,
                pendingSpin = state.pendingSpin,
                onSpinRequested = onSpinRequested,
                onSpinAnimationCompleted = onSpinAnimationCompleted
            )
        }
        item {
            RouletteActionsCard(
                onEditNames = onEditNames,
                onImportMembers = onImportMembers
            )
        }
        item { ActiveNamesCard(names = state.names) }
        item {
            HistoryCard(
                history = state.history,
                onClearHistory = onClearHistory
            )
        }
    }
}

@Composable
private fun RouletteSummaryCard(state: RouletteUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JemaatPill(
                    text = stringResource(id = R.string.roulette_names_count, state.names.size),
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
                JemaatPill(
                    text = sourceLabel(source = state.source),
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = stringResource(id = R.string.roulette_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = stringResource(id = R.string.roulette_source_label, sourceLabel(source = state.source)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
            )
            state.lastUpdatedMillis?.let { updatedAt ->
                Text(
                    text = stringResource(id = R.string.roulette_last_updated, updatedAt.asDisplayDateTime()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
                )
            }
        }
    }
}

@Composable
private fun RouletteActionsCard(
    onEditNames: () -> Unit,
    onImportMembers: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onEditNames,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.roulette_action_edit_names))
            }
            OutlinedButton(
                onClick = onImportMembers,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.GroupAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.roulette_action_import_members))
            }
        }
    }
}

@Composable
private fun ActiveNamesCard(names: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.roulette_participant_count),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (names.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.roulette_names_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(names, key = { it }) { name ->
                        JemaatPill(text = name)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    history: List<RouletteHistoryItem>,
    onClearHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.roulette_history_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (history.isNotEmpty()) {
                    Text(
                        text = stringResource(id = R.string.roulette_action_clear_history),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onClearHistory)
                    )
                }
            }
            if (history.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.roulette_history_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    history.forEach { item ->
                        HistoryRow(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(item: RouletteHistoryItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.winnerName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.drawnAtMillis.asDisplayDateTime(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun ImportSessionCard(
    session: SessionState,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.roulette_import_session_label, session.displayName),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.roulette_import_sector_label, session.sectorContext.sectorName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onRefresh,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(id = R.string.roulette_action_refresh_members))
                }
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(id = R.string.roulette_action_logout))
                }
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
internal fun EmptyMembersCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = stringResource(id = R.string.roulette_import_members_empty),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun MemberSelectionRow(
    member: MemberDetail,
    selected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = member.familyGroup.ifBlank { member.roleInSector },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun sourceLabel(source: RouletteNameSource): String {
    return when (source) {
        RouletteNameSource.MANUAL -> stringResource(id = R.string.roulette_source_manual)
        RouletteNameSource.IMPORT -> stringResource(id = R.string.roulette_source_import)
    }
}

internal fun Long.asDisplayDateTime(): String {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID"))
                .format(Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()))
        } else {
            SimpleDateFormat("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID"))
                .format(Date(this))
        }
    }.getOrDefault("-")
}

// created by Mories Deo Hutapea, S.E.,S.Kom
