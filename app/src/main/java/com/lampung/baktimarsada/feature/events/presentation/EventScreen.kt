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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.EventProgramItem
import com.lampung.baktimarsada.domain.model.ProgramItemType
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.WorshipTemplate
import com.lampung.baktimarsada.domain.model.WorshipTemplateItem
import com.lampung.baktimarsada.domain.repository.AuthRepository
import com.lampung.baktimarsada.domain.repository.EventRepository
import com.lampung.baktimarsada.domain.repository.WorshipTemplateRepository
import com.lampung.baktimarsada.ui.component.BaktiDropdown
import com.lampung.baktimarsada.ui.component.BaktiEmptyState
import com.lampung.baktimarsada.ui.component.BaktiErrorState
import com.lampung.baktimarsada.ui.component.BaktiLoadingState
import com.lampung.baktimarsada.ui.component.BaktiMultilineInput
import com.lampung.baktimarsada.ui.component.BaktiPullToRefreshBox
import com.lampung.baktimarsada.ui.component.BaktiScrollableStateView
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import com.lampung.baktimarsada.ui.component.BaktiValueRow
import com.lampung.baktimarsada.ui.component.BaktiToolbar
import com.lampung.baktimarsada.ui.component.JemaatInfoCard
import com.lampung.baktimarsada.ui.component.JemaatPill
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun EventRoute(
    isAdmin: Boolean,
    session: SessionState,
    onOpenDetail: (String) -> Unit,
    onOpenCreate: () -> Unit = {},
    onOpenEdit: (String) -> Unit = {},
    viewModel: EventViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EventContent(
        isAdmin = isAdmin,
        state = state,
        onRefresh = viewModel::refresh,
        onDelete = viewModel::delete,
        onShowCreate = onOpenCreate,
        onShowDetail = { onOpenDetail(it.id) },
        onShowEdit = { onOpenEdit(it.id) }
    )
}

@Composable
fun EventCreateRoute(
    session: SessionState,
    onBack: () -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EventFormScreen(
        title = stringResource(id = R.string.dialog_add_event),
        initial = null,
        session = session,
        templates = state.templates,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onBack = onBack,
        onSave = viewModel::save,
        onSaveTemplate = viewModel::saveTemplate,
        onDeleteTemplate = viewModel::deleteTemplate
    )
}

@Composable
fun EventEditRoute(
    eventId: String,
    session: SessionState,
    onBack: () -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val initial = state.items.firstOrNull { it.id == eventId }

    EventFormScreen(
        title = stringResource(id = R.string.dialog_edit_event),
        initial = initial,
        session = session,
        templates = state.templates,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onBack = onBack,
        onSave = viewModel::save,
        onSaveTemplate = viewModel::saveTemplate,
        onDeleteTemplate = viewModel::deleteTemplate
    )
}

@Composable
private fun EventFormScreen(
    title: String,
    initial: EventDetail?,
    session: SessionState,
    templates: List<WorshipTemplate>,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSave: (EventDetail) -> Unit,
    onSaveTemplate: (WorshipTemplate) -> Unit,
    onDeleteTemplate: (String) -> Unit
) {
    var eventTitle by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var schedule by rememberSaveable(initial?.id) { mutableStateOf(initial?.scheduledAt.orEmpty()) }
    var location by rememberSaveable(initial?.id) { mutableStateOf(initial?.location.orEmpty()) }
    var description by rememberSaveable(initial?.id) { mutableStateOf(initial?.description.orEmpty()) }
    var programItems by remember(initial?.id) {
        mutableStateOf(initial?.programItems?.sortedBy { it.orderIndex }.orEmpty())
    }
    val isCreateMode = initial == null
    var showTemplateMenu by rememberSaveable { mutableStateOf(false) }
    var showTemplateManager by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(initial?.id) {
        if (initial != null) {
            schedule = initial.scheduledAt
            location = initial.location
            description = initial.description
            eventTitle = initial.title
            programItems = initial.programItems.sortedBy { it.orderIndex }
        }
    }

    Scaffold(
        topBar = {
            BaktiToolbar(
                title = title,
                onBack = onBack,
                actions = {
                    if (isCreateMode) {
                        IconButton(onClick = { showTemplateMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(id = R.string.action_manage_templates)
                            )
                        }
                        DropdownMenu(
                            expanded = showTemplateMenu,
                            onDismissRequest = { showTemplateMenu = false }
                        ) {
                            templates.forEach { template ->
                                DropdownMenuItem(
                                    text = { Text(text = template.title) },
                                    onClick = {
                                        showTemplateMenu = false
                                        eventTitle = template.title
                                        description = template.description
                                        programItems = template.items.mapIndexed { index, item ->
                                            item.toEventProgramItem(eventId = "", orderIndex = index)
                                        }
                                    }
                                )
                            }
                            if (templates.isNotEmpty()) {
                                HorizontalDivider()
                            }
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.action_manage_templates)) },
                                onClick = {
                                    showTemplateMenu = false
                                    showTemplateManager = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (errorMessage != null) {
                BaktiSectionMessage(message = errorMessage)
            }
            BaktiTextInput(
                value = eventTitle,
                label = stringResource(id = R.string.form_title),
                onValueChange = { eventTitle = it }
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
            ProgramItemEditor(
                items = programItems,
                onItemsChanged = { programItems = it },
                emptyLabel = stringResource(id = R.string.program_empty)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text(text = stringResource(id = R.string.action_cancel))
                }
            Button(
                onClick = {
                    onSave(
                        EventDetail(
                            id = initial?.id.orEmpty(),
                            title = eventTitle.trim(),
                            description = description.trim(),
                            scheduledAt = schedule.trim(),
                            location = location.trim(),
                            sectorId = session.sectorContext.sectorId,
                            sectorName = session.sectorContext.sectorName,
                            programItems = programItems.normalizedForEvent(initial?.id.orEmpty())
                        )
                    )
                    onBack()
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading && eventTitle.isNotBlank() && schedule.isNotBlank() && location.isNotBlank()
            ) {
                Text(text = stringResource(id = R.string.action_save))
            }
            }
        }
    }

    if (isCreateMode && showTemplateManager) {
        TemplateManagerDialog(
            state = EventUiState(templates = templates),
            session = session,
            onDismiss = { showTemplateManager = false },
            onSave = onSaveTemplate,
            onDelete = onDeleteTemplate
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

    BaktiPullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading && state.items.isEmpty() -> {
                BaktiScrollableStateView { BaktiLoadingState() }
            }
            state.errorMessage != null && state.items.isEmpty() -> {
                BaktiScrollableStateView {
                    BaktiErrorState(message = state.errorMessage, onRetry = onRefresh)
                }
            }
            state.items.isEmpty() -> {
                BaktiScrollableStateView {
                    BaktiEmptyState(message = stringResource(id = R.string.event_empty))
                }
            }
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
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.semantics { testTag = "event_title" }
                            ) {
                                Text(
                                    text = stringResource(id = R.string.event_title_jemaat),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = stringResource(id = R.string.jemaat_count_events, state.items.size),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    state.errorMessage?.let { message ->
                        item { BaktiSectionMessage(message = message) }
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
}

@Composable
private fun ProgramItemEditor(
    items: List<EventProgramItem>,
    onItemsChanged: (List<EventProgramItem>) -> Unit,
    emptyLabel: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(id = R.string.program_section_title),
            style = MaterialTheme.typography.titleSmall
        )
        if (items.isEmpty()) {
            Text(
                text = emptyLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items.sortedBy { it.orderIndex }.forEachIndexed { index, item ->
            ProgramItemCard(
                index = index,
                item = item,
                onChange = { changed ->
                    onItemsChanged(items.replaceAt(index, changed).normalizedOrder())
                },
                onMoveUp = {
                    if (index > 0) onItemsChanged(items.move(index, index - 1).normalizedOrder())
                },
                onMoveDown = {
                    if (index < items.lastIndex) onItemsChanged(items.move(index, index + 1).normalizedOrder())
                },
                onDelete = { onItemsChanged(items.filterIndexed { itemIndex, _ -> itemIndex != index }.normalizedOrder()) }
            )
        }
        OutlinedButton(
            onClick = {
                onItemsChanged(
                    (items + EventProgramItem(
                        id = "",
                        eventId = "",
                        orderIndex = items.size,
                        title = "",
                        content = "",
                        leader = "",
                        type = ProgramItemType.CUSTOM
                    )).normalizedOrder()
                )
            }
        ) {
            Text(text = stringResource(id = R.string.action_add_program_item))
        }
    }
}

@Composable
private fun ProgramItemCard(
    index: Int,
    item: EventProgramItem,
    onChange: (EventProgramItem) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.program_item_number, index + 1),
                style = MaterialTheme.typography.labelLarge
            )
            val typeOptions = listOf(
                ProgramItemType.OPENING to stringResource(id = R.string.program_type_opening),
                ProgramItemType.SONG to stringResource(id = R.string.program_type_song),
                ProgramItemType.PRAYER to stringResource(id = R.string.program_type_prayer),
                ProgramItemType.SCRIPTURE to stringResource(id = R.string.program_type_scripture),
                ProgramItemType.SERMON to stringResource(id = R.string.program_type_sermon),
                ProgramItemType.OFFERING to stringResource(id = R.string.program_type_offering),
                ProgramItemType.ANNOUNCEMENT to stringResource(id = R.string.program_type_announcement),
                ProgramItemType.CLOSING to stringResource(id = R.string.program_type_closing),
                ProgramItemType.CUSTOM to stringResource(id = R.string.program_type_custom)
            )
            BaktiDropdown(
                selectedValue = typeOptions.firstOrNull { it.first == item.type }?.second.orEmpty(),
                label = stringResource(id = R.string.form_program_type),
                options = typeOptions.map { it.second },
                onValueSelected = { selected ->
                    onChange(item.copy(type = typeOptions.firstOrNull { it.second == selected }?.first ?: ProgramItemType.CUSTOM))
                }
            )
            when (item.type) {
                ProgramItemType.SCRIPTURE -> {
                    BaktiTextInput(
                        value = item.scriptureReference,
                        label = stringResource(id = R.string.form_program_scripture_reference),
                        onValueChange = { onChange(item.copy(scriptureReference = it)) }
                    )
                    BaktiMultilineInput(
                        value = item.scriptureText,
                        label = stringResource(id = R.string.form_program_scripture_text),
                        onValueChange = { onChange(item.copy(scriptureText = it, content = it)) }
                    )
                }
                ProgramItemType.SERMON -> {
                    BaktiTextInput(
                        value = item.title,
                        label = stringResource(id = R.string.form_program_title),
                        onValueChange = { onChange(item.copy(title = it)) }
                    )
                }
                ProgramItemType.SONG -> {
                    BaktiTextInput(
                        value = item.title,
                        label = stringResource(id = R.string.form_program_song_title),
                        onValueChange = { onChange(item.copy(title = it)) }
                    )
                    BaktiMultilineInput(
                        value = item.content,
                        label = stringResource(id = R.string.form_program_song_content),
                        onValueChange = { onChange(item.copy(content = it)) }
                    )
                }
                ProgramItemType.PRAYER -> {
                    BaktiTextInput(
                        value = item.title,
                        label = stringResource(id = R.string.form_program_title),
                        onValueChange = { onChange(item.copy(title = it)) }
                    )
                    BaktiTextInput(
                        value = item.leader,
                        label = stringResource(id = R.string.form_program_prayer_leader),
                        onValueChange = { onChange(item.copy(leader = it)) }
                    )
                    BaktiMultilineInput(
                        value = item.content,
                        label = stringResource(id = R.string.form_program_prayer_content),
                        onValueChange = { onChange(item.copy(content = it)) }
                    )
                }
                ProgramItemType.ANNOUNCEMENT -> {
                    BaktiTextInput(
                        value = item.title,
                        label = stringResource(id = R.string.form_program_title),
                        onValueChange = { onChange(item.copy(title = it)) }
                    )
                    BaktiMultilineInput(
                        value = item.content,
                        label = stringResource(id = R.string.form_program_announcement_content),
                        onValueChange = { onChange(item.copy(content = it)) }
                    )
                }
                ProgramItemType.OFFERING -> {
                    BaktiTextInput(
                        value = item.title,
                        label = stringResource(id = R.string.form_program_title),
                        onValueChange = { onChange(item.copy(title = it)) }
                    )
                    BaktiTextInput(
                        value = item.leader,
                        label = stringResource(id = R.string.form_program_leader),
                        onValueChange = { onChange(item.copy(leader = it)) }
                    )
                    BaktiMultilineInput(
                        value = item.note,
                        label = stringResource(id = R.string.form_program_note),
                        onValueChange = { onChange(item.copy(note = it)) }
                    )
                }
                ProgramItemType.OPENING,
                ProgramItemType.CLOSING,
                ProgramItemType.CUSTOM -> {
                    BaktiTextInput(
                        value = item.title,
                        label = stringResource(id = R.string.form_program_title),
                        onValueChange = { onChange(item.copy(title = it)) }
                    )
                    BaktiTextInput(
                        value = item.leader,
                        label = stringResource(id = R.string.form_program_leader),
                        onValueChange = { onChange(item.copy(leader = it)) }
                    )
                    BaktiMultilineInput(
                        value = item.content,
                        label = stringResource(id = R.string.form_program_content),
                        onValueChange = { onChange(item.copy(content = it)) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onMoveUp, enabled = index > 0) {
                    Text(text = stringResource(id = R.string.action_move_up))
                }
                OutlinedButton(onClick = onMoveDown) {
                    Text(text = stringResource(id = R.string.action_move_down))
                }
                TextButton(onClick = onDelete) {
                    Text(text = stringResource(id = R.string.action_delete))
                }
            }
        }
    }
}

@Composable
private fun TemplateManagerDialog(
    state: EventUiState,
    session: SessionState,
    onDismiss: () -> Unit,
    onSave: (WorshipTemplate) -> Unit,
    onDelete: (String) -> Unit
) {
    var editTarget by remember { mutableStateOf<WorshipTemplate?>(null) }
    var showForm by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.template_manager_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.templateErrorMessage?.let { BaktiSectionMessage(message = it) }
                if (state.templates.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.template_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                state.templates.forEach { template ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = template.title, style = MaterialTheme.typography.titleMedium)
                            Text(text = template.description, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = stringResource(id = R.string.template_item_count, template.items.size),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        editTarget = template
                                        showForm = true
                                    }
                                ) {
                                    Text(text = stringResource(id = R.string.action_edit))
                                }
                                TextButton(onClick = { onDelete(template.id) }) {
                                    Text(text = stringResource(id = R.string.action_delete))
                                }
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.action_close))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    editTarget = null
                    showForm = true
                }
            ) {
                Text(text = stringResource(id = R.string.action_add_template))
            }
        }
    )

    if (showForm) {
        TemplateFormDialog(
            initial = editTarget,
            session = session,
            onDismiss = {
                showForm = false
                editTarget = null
            },
            onSave = {
                onSave(it)
                showForm = false
                editTarget = null
            }
        )
    }
}

@Composable
private fun TemplateFormDialog(
    initial: WorshipTemplate?,
    session: SessionState,
    onDismiss: () -> Unit,
    onSave: (WorshipTemplate) -> Unit
) {
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var description by rememberSaveable(initial?.id) { mutableStateOf(initial?.description.orEmpty()) }
    var programItems by remember(initial?.id) {
        mutableStateOf(initial?.items?.map { it.toEventProgramItem(eventId = "", orderIndex = it.orderIndex) }.orEmpty())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    id = if (initial == null) R.string.dialog_add_template else R.string.dialog_edit_template
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
                BaktiMultilineInput(
                    value = description,
                    label = stringResource(id = R.string.form_description),
                    onValueChange = { description = it }
                )
                ProgramItemEditor(
                    items = programItems,
                    onItemsChanged = { programItems = it },
                    emptyLabel = stringResource(id = R.string.template_program_empty)
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
                    val templateId = initial?.id.orEmpty()
                    onSave(
                        WorshipTemplate(
                            id = templateId,
                            tenantId = session.tenantContext.tenantId,
                            sectorId = session.sectorContext.sectorId,
                            title = title.trim(),
                            description = description.trim(),
                            items = programItems.normalizedForTemplate(templateId)
                        )
                    )
                },
                enabled = title.isNotBlank()
            ) {
                Text(text = stringResource(id = R.string.action_save))
            }
        }
    )
}

data class EventUiState(
    val items: List<EventDetail> = emptyList(),
    val templates: List<WorshipTemplate> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val templateErrorMessage: String? = null
)

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    private val templateRepository: WorshipTemplateRepository,
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
        viewModelScope.launch(dispatcherProvider.io) {
            templateRepository.observeTemplates().collect { templates ->
                _state.update { it.copy(templates = templates) }
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
            when (val result = templateRepository.refresh(session.sectorContext)) {
                is AppResult.Success -> _state.update { it.copy(templateErrorMessage = null) }
                is AppResult.Error -> _state.update { it.copy(templateErrorMessage = result.message) }
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

    fun saveTemplate(template: WorshipTemplate) {
        viewModelScope.launch(dispatcherProvider.io) {
            val session = authRepository.getCurrentSession() ?: return@launch
            _state.update { it.copy(templateErrorMessage = null) }
            when (val result = templateRepository.save(template, session.sectorContext)) {
                is AppResult.Success -> _state.update { it.copy(templateErrorMessage = null) }
                is AppResult.Error -> _state.update { it.copy(templateErrorMessage = result.message) }
            }
        }
    }

    fun deleteTemplate(id: String) {
        viewModelScope.launch(dispatcherProvider.io) {
            val session = authRepository.getCurrentSession() ?: return@launch
            _state.update { it.copy(templateErrorMessage = null) }
            when (val result = templateRepository.delete(id, session.sectorContext)) {
                is AppResult.Success -> _state.update { it.copy(templateErrorMessage = null) }
                is AppResult.Error -> _state.update { it.copy(templateErrorMessage = result.message) }
            }
        }
    }
}

private fun WorshipTemplateItem.toEventProgramItem(eventId: String, orderIndex: Int): EventProgramItem {
    return EventProgramItem(
        id = "",
        eventId = eventId,
        orderIndex = orderIndex,
        title = title,
        content = content,
        leader = leader,
        type = type,
        scriptureReference = scriptureReference,
        scriptureText = scriptureText,
        note = note
    )
}

private fun List<EventProgramItem>.replaceAt(index: Int, item: EventProgramItem): List<EventProgramItem> {
    return mapIndexed { itemIndex, current -> if (itemIndex == index) item else current }
}

private fun List<EventProgramItem>.move(fromIndex: Int, toIndex: Int): List<EventProgramItem> {
    return toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
}

private fun List<EventProgramItem>.normalizedOrder(): List<EventProgramItem> {
    return mapIndexed { index, item -> item.copy(orderIndex = index) }
}

private fun List<EventProgramItem>.normalizedForEvent(eventId: String): List<EventProgramItem> {
    return normalizedOrder().mapIndexedNotNull { index, item ->
        if (item.isBlankProgramItem()) {
            null
        } else {
            item.copy(
                id = item.id.ifBlank { if (eventId.isBlank()) "" else "$eventId-program-$index" },
                eventId = eventId,
                orderIndex = index,
                title = item.title.trim(),
                content = item.content.trim(),
                leader = item.leader.trim(),
                scriptureReference = item.scriptureReference.trim(),
                scriptureText = item.scriptureText.trim(),
                note = item.note.trim()
            )
        }
    }
}

private fun List<EventProgramItem>.normalizedForTemplate(templateId: String): List<WorshipTemplateItem> {
    return normalizedOrder().mapIndexedNotNull { index, item ->
        if (item.isBlankProgramItem()) {
            null
        } else {
            WorshipTemplateItem(
                id = item.id.ifBlank { if (templateId.isBlank()) "" else "$templateId-item-$index" },
                templateId = templateId,
                orderIndex = index,
                title = item.title.trim(),
                content = item.content.trim(),
                leader = item.leader.trim(),
                type = item.type,
                scriptureReference = item.scriptureReference.trim(),
                scriptureText = item.scriptureText.trim(),
                note = item.note.trim()
            )
        }
    }
}

private fun EventProgramItem.isBlankProgramItem(): Boolean {
    return title.isBlank() &&
        content.isBlank() &&
        leader.isBlank() &&
        scriptureReference.isBlank() &&
        scriptureText.isBlank() &&
        note.isBlank()
}

// created by Mories Deo Hutapea, S.E.,S.Kom
