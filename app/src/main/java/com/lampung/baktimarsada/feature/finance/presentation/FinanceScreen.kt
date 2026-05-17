package com.lampung.baktimarsada.feature.finance.presentation

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
import com.lampung.baktimarsada.domain.model.FinanceReportDetail
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.repository.AuthRepository
import com.lampung.baktimarsada.domain.repository.FinanceReportRepository
import com.lampung.baktimarsada.ui.component.BaktiAmountInput
import com.lampung.baktimarsada.ui.component.BaktiCheckbox
import com.lampung.baktimarsada.ui.component.BaktiEmptyState
import com.lampung.baktimarsada.ui.component.BaktiErrorState
import com.lampung.baktimarsada.ui.component.BaktiLoadingState
import com.lampung.baktimarsada.ui.component.BaktiMultilineInput
import com.lampung.baktimarsada.ui.component.BaktiPullToRefreshBox
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.BaktiScrollableStateView
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import com.lampung.baktimarsada.ui.component.BaktiValueRow
import com.lampung.baktimarsada.ui.component.JemaatInfoCard
import com.lampung.baktimarsada.ui.component.JemaatPill
import com.lampung.baktimarsada.ui.component.JemaatSectionHeader
import com.lampung.baktimarsada.ui.util.formatCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun FinanceRoute(
    isAdmin: Boolean,
    session: SessionState,
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var detailTarget by remember { mutableStateOf<FinanceReportDetail?>(null) }
    var editTarget by remember { mutableStateOf<FinanceReportDetail?>(null) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    FinanceContent(
        isAdmin = isAdmin,
        state = state,
        onRefresh = viewModel::refresh,
        onDelete = viewModel::delete,
        onToggleVisibility = viewModel::toggleVisibility,
        onShowCreate = { showCreateDialog = true },
        onShowDetail = { detailTarget = it },
        onShowEdit = { editTarget = it }
    )

    detailTarget?.let { item ->
        FinanceDetailDialog(item = item, onDismiss = { detailTarget = null })
    }

    if (showCreateDialog) {
        FinanceFormDialog(
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
        FinanceFormDialog(
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
fun FinanceContent(
    isAdmin: Boolean,
    state: FinanceUiState,
    onRefresh: () -> Unit,
    onDelete: (String) -> Unit,
    onToggleVisibility: (FinanceReportDetail) -> Unit,
    onShowCreate: () -> Unit,
    onShowDetail: (FinanceReportDetail) -> Unit,
    onShowEdit: (FinanceReportDetail) -> Unit
) {
    val displayItems = if (isAdmin) state.items else state.items.filter { it.isVisibleToJemaat }

    BaktiPullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading && displayItems.isEmpty() -> {
                BaktiScrollableStateView { BaktiLoadingState() }
            }
            state.errorMessage != null && displayItems.isEmpty() -> {
                BaktiScrollableStateView {
                    BaktiErrorState(message = state.errorMessage, onRetry = onRefresh)
                }
            }
            displayItems.isEmpty() -> {
                BaktiScrollableStateView {
                    BaktiEmptyState(message = stringResource(id = R.string.finance_empty))
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
                                text = stringResource(id = R.string.finance_title_admin),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.semantics { testTag = "finance_title" }
                            )
                        } else {
                            JemaatSectionHeader(
                                title = stringResource(id = R.string.finance_title_jemaat),
                                countLabel = stringResource(id = R.string.jemaat_count_finance, displayItems.size),
                                actionLabel = stringResource(id = R.string.action_refresh),
                                onAction = onRefresh,
                                modifier = Modifier.semantics { testTag = "finance_title" }
                            )
                        }
                    }
                    state.errorMessage?.let { message ->
                        item { BaktiSectionMessage(message = message) }
                    }
                    if (isAdmin) item {
                        OutlinedButton(
                            onClick = onRefresh,
                            modifier = Modifier.semantics { testTag = "finance_refresh_button" }
                        ) {
                            Text(text = stringResource(id = R.string.action_refresh))
                        }
                    }
                    items(displayItems, key = { it.id }) { item ->
                        if (isAdmin) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { testTag = "finance_item_${item.id}" }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                                    BaktiValueRow(
                                        label = stringResource(id = R.string.form_period),
                                        value = item.periodLabel
                                    )
                                    BaktiValueRow(
                                        label = stringResource(id = R.string.form_amount),
                                        value = formatCurrency(item.amount)
                                    )
                                    BaktiValueRow(
                                        label = stringResource(id = R.string.form_visibility),
                                        value = stringResource(
                                            id = if (item.isVisibleToJemaat) {
                                                R.string.visibility_visible
                                            } else {
                                                R.string.visibility_hidden
                                            }
                                        )
                                    )
                                    Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onShowDetail(item) },
                                            modifier = Modifier.semantics { testTag = "finance_detail_${item.id}" }
                                        ) {
                                            Text(text = stringResource(id = R.string.action_detail))
                                        }
                                        OutlinedButton(
                                            onClick = { onShowEdit(item) },
                                            modifier = Modifier.semantics { testTag = "finance_edit_${item.id}" }
                                        ) {
                                            Text(text = stringResource(id = R.string.action_edit))
                                        }
                                        OutlinedButton(
                                            onClick = { onToggleVisibility(item) },
                                            modifier = Modifier.semantics { testTag = "finance_toggle_${item.id}" }
                                        ) {
                                            Text(text = stringResource(id = R.string.action_toggle_visibility))
                                        }
                                        Button(
                                            onClick = { onDelete(item.id) },
                                            modifier = Modifier.semantics { testTag = "finance_delete_${item.id}" }
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
                                modifier = Modifier.semantics { testTag = "finance_item_${item.id}" },
                                accentColor = MaterialTheme.colorScheme.tertiary,
                                content = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = formatCurrency(item.amount),
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                        JemaatPill(text = item.periodLabel)
                                    }
                                },
                                footer = {
                                    OutlinedButton(
                                        onClick = { onShowDetail(item) },
                                        modifier = Modifier.semantics { testTag = "finance_detail_${item.id}" }
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
                    .semantics { testTag = "finance_add_fab" },
                onClick = onShowCreate
            ) {
                Text(text = stringResource(id = R.string.action_add_finance))
            }
        }
    }
}
}

@Composable
private fun FinanceDetailDialog(
    item: FinanceReportDetail,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.action_detail)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BaktiValueRow(label = stringResource(id = R.string.form_title), value = item.title)
                BaktiValueRow(label = stringResource(id = R.string.form_period), value = item.periodLabel)
                BaktiValueRow(label = stringResource(id = R.string.form_amount), value = formatCurrency(item.amount))
                BaktiValueRow(
                    label = stringResource(id = R.string.form_visibility),
                    value = stringResource(
                        id = if (item.isVisibleToJemaat) R.string.visibility_visible else R.string.visibility_hidden
                    )
                )
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
private fun FinanceFormDialog(
    initial: FinanceReportDetail?,
    session: SessionState,
    onDismiss: () -> Unit,
    onSave: (FinanceReportDetail) -> Unit
) {
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var period by rememberSaveable(initial?.id) { mutableStateOf(initial?.periodLabel.orEmpty()) }
    var amount by rememberSaveable(initial?.id) { mutableStateOf(initial?.amount?.toString().orEmpty()) }
    var description by rememberSaveable(initial?.id) { mutableStateOf(initial?.description.orEmpty()) }
    var isVisible by rememberSaveable(initial?.id) { mutableStateOf(initial?.isVisibleToJemaat ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    id = if (initial == null) R.string.dialog_add_finance else R.string.dialog_edit_finance
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
                    value = period,
                    label = stringResource(id = R.string.form_period),
                    onValueChange = { period = it }
                )
                BaktiAmountInput(
                    value = amount,
                    label = stringResource(id = R.string.form_amount),
                    onValueChange = { amount = it }
                )
                BaktiMultilineInput(
                    value = description,
                    label = stringResource(id = R.string.form_description),
                    onValueChange = { description = it }
                )
                BaktiCheckbox(
                    checked = isVisible,
                    label = stringResource(id = R.string.form_visible_to_jemaat),
                    onCheckedChange = { isVisible = it }
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
                        FinanceReportDetail(
                            id = initial?.id.orEmpty(),
                            title = title.trim(),
                            description = description.trim(),
                            periodLabel = period.trim(),
                            amount = amount.toLongOrNull() ?: 0L,
                            isVisibleToJemaat = isVisible,
                            sectorId = session.sectorContext.sectorId,
                            sectorName = session.sectorContext.sectorName
                        )
                    )
                },
                enabled = title.isNotBlank() && period.isNotBlank() && amount.isNotBlank()
            ) {
                Text(text = stringResource(id = R.string.action_save))
            }
        }
    )
}

data class FinanceUiState(
    val items: List<FinanceReportDetail> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val repository: FinanceReportRepository,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _state = MutableStateFlow(FinanceUiState())
    val state: StateFlow<FinanceUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            repository.observeReports().collect { items ->
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

    fun save(item: FinanceReportDetail) {
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

    fun toggleVisibility(item: FinanceReportDetail) {
        viewModelScope.launch(dispatcherProvider.io) {
            val session = authRepository.getCurrentSession() ?: return@launch
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.toggleVisibility(item, session.sectorContext)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, errorMessage = null) }
                is AppResult.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }
}
