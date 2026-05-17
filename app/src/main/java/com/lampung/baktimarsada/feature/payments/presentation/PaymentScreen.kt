package com.lampung.baktimarsada.feature.payments.presentation

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
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.domain.model.PaymentStatus
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.repository.AuthRepository
import com.lampung.baktimarsada.domain.repository.MemberRepository
import com.lampung.baktimarsada.domain.repository.PaymentObligationRepository
import com.lampung.baktimarsada.ui.component.BaktiAmountInput
import com.lampung.baktimarsada.ui.component.BaktiDropdown
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
import com.lampung.baktimarsada.ui.component.PaymentStatusChip
import com.lampung.baktimarsada.ui.util.formatCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun PaymentRoute(
    isAdmin: Boolean,
    session: SessionState,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var detailTarget by remember { mutableStateOf<PaymentObligationDetail?>(null) }
    var editTarget by remember { mutableStateOf<PaymentObligationDetail?>(null) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    PaymentContent(
        isAdmin = isAdmin,
        state = state,
        onRefresh = viewModel::refresh,
        onDelete = viewModel::delete,
        onShowCreate = { showCreateDialog = true },
        onShowDetail = { detailTarget = it },
        onShowEdit = { editTarget = it }
    )

    detailTarget?.let { item ->
        PaymentDetailDialog(item = item, onDismiss = { detailTarget = null })
    }

    if (showCreateDialog) {
        PaymentFormDialog(
            initial = null,
            session = session,
            memberOptions = state.members,
            onDismiss = { showCreateDialog = false },
            onSave = {
                viewModel.save(it)
                showCreateDialog = false
            }
        )
    }

    editTarget?.let { item ->
        PaymentFormDialog(
            initial = item,
            session = session,
            memberOptions = state.members,
            onDismiss = { editTarget = null },
            onSave = {
                viewModel.save(it)
                editTarget = null
            }
        )
    }
}

@Composable
fun PaymentContent(
    isAdmin: Boolean,
    state: PaymentUiState,
    onRefresh: () -> Unit,
    onDelete: (String) -> Unit,
    onShowCreate: () -> Unit,
    onShowDetail: (PaymentObligationDetail) -> Unit,
    onShowEdit: (PaymentObligationDetail) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var visibleCount by rememberSaveable { mutableStateOf(AppConstants.DEFAULT_LIST_PAGE_SIZE) }
    val filteredItems = state.items.filter { it.matchesPaymentQuery(searchQuery) }
    val displayItems = filteredItems.take(visibleCount)
    val canLoadMore = displayItems.size < filteredItems.size

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
            filteredItems.isEmpty() -> {
                BaktiScrollableStateView {
                    BaktiEmptyState(message = stringResource(id = R.string.payment_empty))
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
                        BaktiTextInput(
                            value = searchQuery,
                            label = stringResource(id = R.string.search_payments),
                            onValueChange = {
                                searchQuery = it
                                visibleCount = AppConstants.DEFAULT_LIST_PAGE_SIZE
                            },
                            modifier = Modifier.semantics { testTag = "payment_search_input" }
                        )
                    }
                    item {
                        if (isAdmin) {
                            Text(
                                text = stringResource(id = R.string.payment_title_admin),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.semantics { testTag = "payment_title" }
                            )
                        } else {
                            JemaatSectionHeader(
                                title = stringResource(id = R.string.payment_title_jemaat),
                                countLabel = stringResource(id = R.string.jemaat_count_payments, filteredItems.size),
                                actionLabel = stringResource(id = R.string.action_refresh),
                                onAction = onRefresh,
                                modifier = Modifier.semantics { testTag = "payment_title" }
                            )
                        }
                    }
                    state.errorMessage?.let { message ->
                        item { BaktiSectionMessage(message = message) }
                    }
                    if (isAdmin) item {
                        OutlinedButton(
                            onClick = onRefresh,
                            modifier = Modifier.semantics { testTag = "payment_refresh_button" }
                        ) {
                            Text(text = stringResource(id = R.string.action_refresh))
                        }
                    }
                    item {
                        Text(
                            text = stringResource(id = R.string.pagination_summary, displayItems.size, filteredItems.size),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    items(displayItems, key = { it.id }) { item ->
                        if (isAdmin) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { testTag = "payment_item_${item.id}" }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                                    BaktiValueRow(
                                        label = stringResource(id = R.string.form_member),
                                        value = item.memberName
                                    )
                                    BaktiValueRow(
                                        label = stringResource(id = R.string.form_due_date),
                                        value = item.dueDate
                                    )
                                    BaktiValueRow(
                                        label = stringResource(id = R.string.form_amount),
                                        value = formatCurrency(item.amount)
                                    )
                                    PaymentStatusChip(
                                        status = item.status,
                                        label = paymentStatusLabel(item.status)
                                    )
                                    Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onShowDetail(item) },
                                            modifier = Modifier.semantics { testTag = "payment_detail_${item.id}" }
                                        ) {
                                            Text(text = stringResource(id = R.string.action_detail))
                                        }
                                        OutlinedButton(
                                            onClick = { onShowEdit(item) },
                                            modifier = Modifier.semantics { testTag = "payment_edit_${item.id}" }
                                        ) {
                                            Text(text = stringResource(id = R.string.action_edit))
                                        }
                                        Button(
                                            onClick = { onDelete(item.id) },
                                            modifier = Modifier.semantics { testTag = "payment_delete_${item.id}" }
                                        ) {
                                            Text(text = stringResource(id = R.string.action_delete))
                                        }
                                    }
                                }
                            }
                        } else {
                            JemaatInfoCard(
                                title = item.title,
                                subtitle = item.memberName,
                                modifier = Modifier.semantics { testTag = "payment_item_${item.id}" },
                                accentColor = when (item.status) {
                                    PaymentStatus.PAID -> MaterialTheme.colorScheme.primary
                                    PaymentStatus.UNPAID -> MaterialTheme.colorScheme.secondary
                                    PaymentStatus.OVERDUE -> MaterialTheme.colorScheme.error
                                },
                                content = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = formatCurrency(item.amount),
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            JemaatPill(text = item.dueDate)
                                            PaymentStatusChip(
                                                status = item.status,
                                                label = paymentStatusLabel(item.status)
                                            )
                                        }
                                    }
                                },
                                footer = {
                                    OutlinedButton(
                                        onClick = { onShowDetail(item) },
                                        modifier = Modifier.semantics { testTag = "payment_detail_${item.id}" }
                                    ) {
                                        Text(text = stringResource(id = R.string.action_detail))
                                    }
                                }
                            )
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

        if (isAdmin) {
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .semantics { testTag = "payment_add_fab" },
                onClick = onShowCreate
            ) {
                Text(text = stringResource(id = R.string.action_add_payment))
            }
        }
    }
    }
}

private fun PaymentObligationDetail.matchesPaymentQuery(query: String): Boolean {
    val keyword = query.trim().lowercase()
    if (keyword.isBlank()) return true
    return title.lowercase().contains(keyword) ||
        description.lowercase().contains(keyword) ||
        memberName.lowercase().contains(keyword) ||
        dueDate.lowercase().contains(keyword) ||
        status.name.lowercase().contains(keyword)
}

@Composable
private fun PaymentDetailDialog(
    item: PaymentObligationDetail,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.action_detail)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BaktiValueRow(label = stringResource(id = R.string.form_title), value = item.title)
                BaktiValueRow(label = stringResource(id = R.string.form_member), value = item.memberName)
                BaktiValueRow(label = stringResource(id = R.string.form_due_date), value = item.dueDate)
                BaktiValueRow(label = stringResource(id = R.string.form_amount), value = formatCurrency(item.amount))
                BaktiValueRow(label = stringResource(id = R.string.form_status), value = paymentStatusLabel(item.status))
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
private fun PaymentFormDialog(
    initial: PaymentObligationDetail?,
    session: SessionState,
    memberOptions: List<MemberDetail>,
    onDismiss: () -> Unit,
    onSave: (PaymentObligationDetail) -> Unit
) {
    val memberLabels = memberOptions.map { it.fullName }
    val initialMember = initial?.memberName.orEmpty()
    var memberName by rememberSaveable(initial?.id) { mutableStateOf(initialMember) }
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var dueDate by rememberSaveable(initial?.id) { mutableStateOf(initial?.dueDate.orEmpty()) }
    var amount by rememberSaveable(initial?.id) { mutableStateOf(initial?.amount?.toString().orEmpty()) }
    var status by rememberSaveable(initial?.id) { mutableStateOf(initial?.status ?: PaymentStatus.UNPAID) }
    var description by rememberSaveable(initial?.id) { mutableStateOf(initial?.description.orEmpty()) }
    val statusOptions = listOf(
        PaymentStatus.UNPAID to stringResource(id = R.string.payment_status_unpaid),
        PaymentStatus.PAID to stringResource(id = R.string.payment_status_paid),
        PaymentStatus.OVERDUE to stringResource(id = R.string.payment_status_overdue)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    id = if (initial == null) R.string.dialog_add_payment else R.string.dialog_edit_payment
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BaktiDropdown(
                    selectedValue = memberName,
                    label = stringResource(id = R.string.form_member),
                    options = memberLabels,
                    onValueSelected = { memberName = it }
                )
                BaktiTextInput(
                    value = title,
                    label = stringResource(id = R.string.form_title),
                    onValueChange = { title = it }
                )
                BaktiTextInput(
                    value = dueDate,
                    label = stringResource(id = R.string.form_due_date),
                    onValueChange = { dueDate = it }
                )
                BaktiAmountInput(
                    value = amount,
                    label = stringResource(id = R.string.form_amount),
                    onValueChange = { amount = it }
                )
                BaktiDropdown(
                    selectedValue = paymentStatusLabel(status),
                    label = stringResource(id = R.string.form_status),
                    options = statusOptions.map { it.second },
                    onValueSelected = { selected ->
                        status = statusOptions.first { it.second == selected }.first
                    }
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
            val selectedMember = memberOptions.firstOrNull { it.fullName == memberName }
            Button(
                onClick = {
                    if (selectedMember != null) {
                        onSave(
                            PaymentObligationDetail(
                                id = initial?.id.orEmpty(),
                                memberId = selectedMember.id,
                                memberName = selectedMember.fullName,
                                title = title.trim(),
                                description = description.trim(),
                                amount = amount.toLongOrNull() ?: 0L,
                                dueDate = dueDate.trim(),
                                status = status,
                                sectorId = session.sectorContext.sectorId,
                                sectorName = session.sectorContext.sectorName
                            )
                        )
                    }
                },
                enabled = memberName.isNotBlank() && title.isNotBlank() && amount.isNotBlank() && dueDate.isNotBlank()
            ) {
                Text(text = stringResource(id = R.string.action_save))
            }
        }
    )
}

@Composable
private fun paymentStatusLabel(status: PaymentStatus): String {
    return when (status) {
        PaymentStatus.UNPAID -> stringResource(id = R.string.payment_status_unpaid)
        PaymentStatus.PAID -> stringResource(id = R.string.payment_status_paid)
        PaymentStatus.OVERDUE -> stringResource(id = R.string.payment_status_overdue)
    }
}

data class PaymentUiState(
    val items: List<PaymentObligationDetail> = emptyList(),
    val members: List<MemberDetail> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repository: PaymentObligationRepository,
    private val memberRepository: MemberRepository,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentUiState())
    val state: StateFlow<PaymentUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            repository.observeObligations().collect { items ->
                _state.update { it.copy(items = items, isLoading = false) }
            }
        }
        viewModelScope.launch(dispatcherProvider.io) {
            memberRepository.observeMembers().collect { members ->
                _state.update { it.copy(members = members) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(dispatcherProvider.io) {
            val session = authRepository.getCurrentSession() ?: return@launch
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            memberRepository.refresh(session.sectorContext)
            when (val result = repository.refresh(session.sectorContext)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, errorMessage = null) }
                is AppResult.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun save(item: PaymentObligationDetail) {
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

// created by Mories Deo Hutapea, S.E.,S.Kom
