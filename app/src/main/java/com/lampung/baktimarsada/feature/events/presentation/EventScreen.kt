package com.lampung.baktimarsada.feature.events.presentation

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.lampung.baktimarsada.ui.component.BaktiLoadingState
import com.lampung.baktimarsada.ui.component.BaktiMultilineInput
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import com.lampung.baktimarsada.ui.component.BaktiValueRow
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

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading && state.items.isEmpty() -> BaktiLoadingState()
            state.items.isEmpty() -> BaktiEmptyState(message = stringResource(id = R.string.event_empty))
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(
                                id = if (isAdmin) R.string.event_title_admin else R.string.event_title_jemaat
                            ),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    state.errorMessage?.let { message ->
                        item { BaktiSectionMessage(message = message) }
                    }
                    item {
                        OutlinedButton(onClick = viewModel::refresh) {
                            Text(text = stringResource(id = R.string.action_refresh))
                        }
                    }
                    items(state.items, key = { it.id }) { item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
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
                                    OutlinedButton(onClick = { detailTarget = item }) {
                                        Text(text = stringResource(id = R.string.action_detail))
                                    }
                                    if (isAdmin) {
                                        OutlinedButton(onClick = { editTarget = item }) {
                                            Text(text = stringResource(id = R.string.action_edit))
                                        }
                                        Button(onClick = { viewModel.delete(item.id) }) {
                                            Text(text = stringResource(id = R.string.action_delete))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isAdmin) {
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = { showCreateDialog = true }
            ) {
                Text(text = stringResource(id = R.string.action_add_event))
            }
        }
    }

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
private fun EventDetailDialog(
    item: EventDetail,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.action_detail)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BaktiValueRow(label = stringResource(id = R.string.form_title), value = item.title)
                BaktiValueRow(label = stringResource(id = R.string.form_schedule), value = item.scheduledAt)
                BaktiValueRow(label = stringResource(id = R.string.form_location), value = item.location)
                BaktiValueRow(label = stringResource(id = R.string.form_description), value = item.description)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_close))
            }
        }
    )
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
