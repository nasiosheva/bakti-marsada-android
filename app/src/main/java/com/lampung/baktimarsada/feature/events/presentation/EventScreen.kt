package com.lampung.baktimarsada.feature.events.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.repository.AuthRepository
import com.lampung.baktimarsada.domain.repository.EventRepository
import com.lampung.baktimarsada.ui.component.BaktiEmptyState
import com.lampung.baktimarsada.ui.component.BaktiErrorState
import com.lampung.baktimarsada.ui.component.BaktiLoadingState
import com.lampung.baktimarsada.ui.component.BaktiMultilineInput
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import com.lampung.baktimarsada.ui.component.BaktiValueRow
import com.lampung.baktimarsada.ui.component.JemaatInfoCard
import com.lampung.baktimarsada.ui.component.JemaatPill
import com.lampung.baktimarsada.ui.component.JemaatSectionHeader
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun EventRoute(
    isAdmin: Boolean,
    session: SessionState,
    viewModel: EventViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var detailTarget by remember { mutableStateOf<EventDetail?>(null) }
    var editTarget by remember { mutableStateOf<EventDetail?>(null) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    EventContent(
        isAdmin = isAdmin,
        state = state,
        onRefresh = viewModel::refresh,
        onDelete = viewModel::delete,
        onShowCreate = { showCreateDialog = true },
        onShowDetail = { detailTarget = it },
        onShowEdit = { editTarget = it }
    )

    detailTarget?.let { item ->
        EventDetailDialog(item = item, onDismiss = { detailTarget = null })
    }

    if (showCreateDialog) {
        EventFormDialog(
            initial = null,
            session = session,
            onDismiss = { showCreateDialog = false },
            onSave = {
                viewModel.save(it)
                showCreateDialog = false
            }
        )
    }

    editTarget?.let { item ->
        EventFormDialog(
            initial = item,
            session = session,
            onDismiss = { editTarget = null },
            onSave = {
                viewModel.save(it)
                editTarget = null
            }
        )
    }
}

@Composable
fun EventContent(
    isAdmin: Boolean,
    state: EventUiState,
    onRefresh: () -> Unit,
    onDelete: (String) -> Unit,
    onShowCreate: () -> Unit,
    onShowDetail: (EventDetail) -> Unit,
    onShowEdit: (EventDetail) -> Unit
) {

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading && state.items.isEmpty() -> BaktiLoadingState()
            state.errorMessage != null && state.items.isEmpty() -> {
                BaktiErrorState(message = state.errorMessage, onRetry = onRefresh)
            }
            state.items.isEmpty() -> BaktiEmptyState(message = stringResource(id = R.string.event_empty))
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        if (isAdmin) {
                            Text(
                                text = stringResource(id = R.string.event_title_admin),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.semantics { testTag = "event_title" }
                            )
                        } else {
                            JemaatSectionHeader(
                                title = stringResource(id = R.string.event_title_jemaat),
                                countLabel = stringResource(id = R.string.jemaat_count_events, state.items.size),
                                actionLabel = stringResource(id = R.string.action_refresh),
                                onAction = onRefresh,
                                modifier = Modifier.semantics { testTag = "event_title" }
                            )
                        }
                    }
                    state.errorMessage?.let { message ->
                        item { BaktiSectionMessage(message = message) }
                    }
                    if (isAdmin) item {
                        OutlinedButton(
                            onClick = onRefresh,
                            modifier = Modifier.semantics { testTag = "event_refresh_button" }
                        ) {
                            Text(text = stringResource(id = R.string.action_refresh))
                        }
                    }
                    items(state.items, key = { it.id }) { item ->
                        if (isAdmin) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { testTag = "event_item_${item.id}" }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                                    BaktiValueRow(
                                        label = stringResource(id = R.string.form_schedule),
                                        value = item.scheduledAt
                                    )
                                    BaktiValueRow(
                                        label = stringResource(id = R.string.form_location),
                                        value = item.location
                                    )
                                    Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onShowDetail(item) },
                                            modifier = Modifier.semantics { testTag = "event_detail_${item.id}" }
                                        ) {
                                            Text(text = stringResource(id = R.string.action_detail))
                                        }
                                        OutlinedButton(
                                            onClick = { onShowEdit(item) },
                                            modifier = Modifier.semantics { testTag = "event_edit_${item.id}" }
                                        ) {
                                            Text(text = stringResource(id = R.string.action_edit))
                                        }
                                        Button(
                                            onClick = { onDelete(item.id) },
                                            modifier = Modifier.semantics { testTag = "event_delete_${item.id}" }
                                        ) {
                                            Text(text = stringResource(id = R.string.action_delete))
                                        }
                                    }
                                }
                            }
                        } else {
                            JemaatInfoCard(
                                title = item.title,
                                subtitle = item.description,
                                modifier = Modifier.semantics { testTag = "event_item_${item.id}" },
                                content = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        JemaatPill(text = item.scheduledAt)
                                        JemaatPill(text = item.location)
                                    }
                                },
                                footer = {
                                    OutlinedButton(
                                        onClick = { onShowDetail(item) },
                                        modifier = Modifier.semantics { testTag = "event_detail_${item.id}" }
                                    ) {
                                        Text(text = stringResource(id = R.string.action_detail))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (isAdmin) {
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .semantics { testTag = "event_add_fab" },
                onClick = onShowCreate
            ) {
                Text(text = stringResource(id = R.string.action_add_event))
            }
        }
    }
}

@Composable
private fun EventDetailDialog(
    item: EventDetail,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(id = R.string.event_detail_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    JemaatPill(text = item.scheduledAt)
                    JemaatPill(text = item.location)
                }
                Card(
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
                    )
                ) {
                    Text(
                        text = item.description,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.action_close))
                }
            }
        }
    }
}

@Composable
private fun EventFormDialog(
    initial: EventDetail?,
    session: SessionState,
    onDismiss: () -> Unit,
    onSave: (EventDetail) -> Unit
) {
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var schedule by rememberSaveable(initial?.id) { mutableStateOf(initial?.scheduledAt.orEmpty()) }
    var location by rememberSaveable(initial?.id) { mutableStateOf(initial?.location.orEmpty()) }
    var description by rememberSaveable(initial?.id) { mutableStateOf(initial?.description.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    id = if (initial == null) R.string.dialog_add_event else R.string.dialog_edit_event
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BaktiTextInput(
                    value = title,
                    label = stringResource(id = R.string.form_title),
                    onValueChange = { title = it }
                )
                BaktiTextInput(
                    value = schedule,
                    label = stringResource(id = R.string.form_schedule),
                    onValueChange = { schedule = it }
                )
                BaktiTextInput(
                    value = location,
                    label = stringResource(id = R.string.form_location),
                    onValueChange = { location = it }
                )
                BaktiMultilineInput(
                    value = description,
                    label = stringResource(id = R.string.form_description),
                    onValueChange = { description = it }
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_cancel))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        EventDetail(
                            id = initial?.id.orEmpty(),
                            title = title.trim(),
                            description = description.trim(),
                            scheduledAt = schedule.trim(),
                            location = location.trim(),
                            sectorId = session.sectorContext.sectorId,
                            sectorName = session.sectorContext.sectorName
                        )
                    )
                },
                enabled = title.isNotBlank() && schedule.isNotBlank() && location.isNotBlank()
            ) {
                Text(text = stringResource(id = R.string.action_save))
            }
        }
    )
}

data class EventUiState(
    val items: List<EventDetail> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _state = MutableStateFlow(EventUiState())
    val state: StateFlow<EventUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            repository.observeEvents().collect { items ->
                _state.update { it.copy(items = items, isLoading = false) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(dispatcherProvider.io) {
            val session = authRepository.getCurrentSession() ?: return@launch
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.refresh(session.sectorContext)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, errorMessage = null) }
                is AppResult.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun save(item: EventDetail) {
        viewModelScope.launch(dispatcherProvider.io) {
            val session = authRepository.getCurrentSession() ?: return@launch
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.save(item, session.sectorContext)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, errorMessage = null) }
                is AppResult.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch(dispatcherProvider.io) {
            val session = authRepository.getCurrentSession() ?: return@launch
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.delete(id, session.sectorContext)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, errorMessage = null) }
                is AppResult.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }
}
