package com.lampung.baktimarsada.feature.events.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.ui.component.BaktiEmptyState
import com.lampung.baktimarsada.ui.component.BaktiLoadingState
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import com.lampung.baktimarsada.ui.component.BaktiToolbar

@Composable
fun EventRoute(
    isAdmin: Boolean,
    session: SessionState,
    onOpenDetail: (String) -> Unit,
    onOpenCreate: () -> Unit = {},
    onOpenEdit: (String) -> Unit = {},
    bottomContentPadding: Dp = 0.dp,
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
        onShowEdit = { onOpenEdit(it.id) },
        bottomContentPadding = bottomContentPadding
    )
}

@Composable
fun EventCreateRoute(
    session: SessionState,
    copyFromEventId: String?,
    onOpenTemplates: () -> Unit,
    onOpenCopyFromPrevious: () -> Unit,
    onBack: () -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val copyFromEvent = state.items.firstOrNull { it.id == copyFromEventId }

    EventFormScreen(
        title = stringResource(id = R.string.dialog_add_event),
        initial = null,
        copyFromEvent = copyFromEvent,
        session = session,
        templates = state.templates,
        previousEvents = state.items,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onOpenTemplates = onOpenTemplates,
        onOpenCopyFromPrevious = onOpenCopyFromPrevious,
        onBack = onBack,
        onSave = viewModel::save
    )
}

@Composable
fun EventCopySourceRoute(
    onBack: () -> Unit,
    onSelectEvent: (String) -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var visibleCount by rememberSaveable { mutableStateOf(AppConstants.DEFAULT_LIST_PAGE_SIZE) }
    val filteredItems = state.items.filter { it.matchesEventQuery(searchQuery) }
    val displayItems = filteredItems.take(visibleCount)
    val canLoadMore = displayItems.size < filteredItems.size

    Scaffold(
        topBar = {
            BaktiToolbar(
                title = stringResource(id = R.string.copy_previous_event_title),
                onBack = onBack
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading && state.items.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) { BaktiLoadingState() }
            }
            filteredItems.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    BaktiEmptyState(message = stringResource(id = R.string.copy_previous_event_empty))
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        BaktiTextInput(
                            value = searchQuery,
                            label = stringResource(id = R.string.search_copy_previous_event),
                            onValueChange = {
                                searchQuery = it
                                visibleCount = AppConstants.DEFAULT_LIST_PAGE_SIZE
                            }
                        )
                    }
                    item {
                        Text(
                            text = stringResource(id = R.string.pagination_summary, displayItems.size, filteredItems.size),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    items(displayItems, key = { it.id }) { event ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onSelectEvent(event.id) }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = event.title, style = MaterialTheme.typography.titleMedium)
                                Text(text = event.scheduledAt, style = MaterialTheme.typography.bodyMedium)
                                Text(text = event.location, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    if (canLoadMore) {
                        item {
                            OutlinedButton(
                                onClick = { visibleCount += AppConstants.DEFAULT_LIST_PAGE_SIZE },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = stringResource(id = R.string.action_load_more))
                            }
                        }
                    }
                }
            }
        }
    }
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
        copyFromEvent = null,
        session = session,
        templates = state.templates,
        previousEvents = emptyList(),
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onOpenTemplates = {},
        onOpenCopyFromPrevious = {},
        onBack = onBack,
        onSave = viewModel::save
    )
}

@Composable
fun EventTemplateRoute(
    session: SessionState,
    onBack: () -> Unit,
    onOpenCreate: () -> Unit,
    onOpenEdit: (String) -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BaktiToolbar(title = stringResource(id = R.string.template_manager_title), onBack = onBack) },
        floatingActionButton = { androidx.compose.material3.ExtendedFloatingActionButton(onClick = onOpenCreate) { Text(stringResource(id = R.string.action_add_template)) } }
    ) { innerPadding ->
        when {
            state.isLoading && state.templates.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) { BaktiLoadingState() }
            }
            state.templates.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    BaktiEmptyState(message = stringResource(id = R.string.template_empty))
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.templateErrorMessage?.let { message -> item { BaktiSectionMessage(message = message) } }
                    items(state.templates, key = { it.id }) { template ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = template.title, style = MaterialTheme.typography.titleMedium)
                                Text(text = template.description, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = stringResource(id = R.string.template_item_count, template.items.size),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onOpenEdit(template.id) }) { Text(text = stringResource(id = R.string.action_edit)) }
                                    androidx.compose.material3.TextButton(onClick = { viewModel.deleteTemplate(template.id) }) {
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
}

@Composable
fun EventTemplateCreateRoute(
    session: SessionState,
    onBack: () -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    EventTemplateFormScreen(
        screenTitle = stringResource(id = R.string.dialog_add_template),
        initial = null,
        session = session,
        onBack = onBack,
        onSave = {
            viewModel.saveTemplate(it)
            onBack()
        }
    )
}

@Composable
fun EventTemplateEditRoute(
    templateId: String,
    session: SessionState,
    onBack: () -> Unit,
    viewModel: EventViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val initial = state.templates.firstOrNull { it.id == templateId }

    EventTemplateFormScreen(
        screenTitle = stringResource(id = R.string.dialog_edit_template),
        initial = initial,
        session = session,
        onBack = onBack,
        onSave = {
            viewModel.saveTemplate(it)
            onBack()
        }
    )
}

// created by Mories Deo Hutapea, S.E.,S.Kom
