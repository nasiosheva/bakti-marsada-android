package com.lampung.baktimarsada.feature.finance.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.model.FinanceReportDetail
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.repository.AuthRepository
import com.lampung.baktimarsada.repository.FinanceReportRepository
import com.lampung.baktimarsada.ui.component.BaktiAmountInput
import com.lampung.baktimarsada.ui.component.BaktiCheckbox
import com.lampung.baktimarsada.ui.component.BaktiEmptyState
import com.lampung.baktimarsada.ui.component.BaktiErrorState
import com.lampung.baktimarsada.ui.component.BaktiLoadingState
import com.lampung.baktimarsada.ui.component.BaktiMultilineInput
import com.lampung.baktimarsada.ui.component.BaktiPullToRefreshBox
import com.lampung.baktimarsada.ui.component.BaktiScrollableStateView
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import com.lampung.baktimarsada.ui.component.JemaatPill
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme
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
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var visibleCount by rememberSaveable { mutableStateOf(AppConstants.DEFAULT_LIST_PAGE_SIZE) }
    val filteredItems = displayItems.filter { it.matchesFinanceQuery(searchQuery) }
    val pagedItems = filteredItems.take(visibleCount)
    val canLoadMore = pagedItems.size < filteredItems.size
    val totalAmount = filteredItems.sumOf { it.amount }
    val title = stringResource(
        id = if (isAdmin) R.string.finance_title_admin else R.string.finance_title_jemaat
    )
    val countLabel = if (isAdmin) {
        stringResource(id = R.string.pagination_summary, pagedItems.size, filteredItems.size)
    } else {
        stringResource(id = R.string.jemaat_count_finance, filteredItems.size)
    }

    BaktiPullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            when {
                state.isLoading && displayItems.isEmpty() -> {
                    BaktiScrollableStateView { BaktiLoadingState() }
                }
                state.errorMessage != null && displayItems.isEmpty() -> {
                    BaktiScrollableStateView {
                        BaktiErrorState(message = state.errorMessage, onRetry = onRefresh)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            FinanceListHeader(
                                title = title,
                                countLabel = countLabel,
                                totalAmount = totalAmount,
                                isAdmin = isAdmin,
                                modifier = Modifier.semantics { testTag = "finance_title" }
                            )
                        }
                        item {
                            FinanceSearchPanel(
                                searchQuery = searchQuery,
                                onSearchChange = {
                                    searchQuery = it
                                    visibleCount = AppConstants.DEFAULT_LIST_PAGE_SIZE
                                }
                            )
                        }
                        state.errorMessage?.let { message ->
                            item { BaktiSectionMessage(message = message) }
                        }
                        if (filteredItems.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                ) {
                                    BaktiEmptyState(message = stringResource(id = R.string.finance_empty))
                                }
                            }
                            return@LazyColumn
                        }
                        items(pagedItems, key = { it.id }) { item ->
                            FinanceListCard(
                                item = item,
                                isAdmin = isAdmin,
                                onShowDetail = onShowDetail,
                                onShowEdit = onShowEdit,
                                onToggleVisibility = onToggleVisibility,
                                onDelete = onDelete,
                                modifier = Modifier.semantics { testTag = "finance_item_${item.id}" }
                            )
                        }
                        if (canLoadMore) {
                            item {
                                OutlinedButton(
                                    onClick = { visibleCount += AppConstants.DEFAULT_LIST_PAGE_SIZE },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
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
                        .semantics { testTag = "finance_add_fab" },
                    onClick = onShowCreate,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(id = R.string.action_add_finance)
                        )
                    },
                    text = { Text(text = stringResource(id = R.string.action_add_finance)) }
                )
            }
        }
    }
}

@Composable
private fun FinanceListHeader(
    title: String,
    countLabel: String,
    totalAmount: Long,
    isAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        JemaatPill(
                            text = if (isAdmin) {
                                stringResource(id = R.string.profile_role_admin)
                            } else {
                                stringResource(id = R.string.profile_role_jemaat)
                            },
                            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = countLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.finance_total_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = formatCurrency(totalAmount),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceSearchPanel(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        BaktiTextInput(
            value = searchQuery,
            label = stringResource(id = R.string.search_finance),
            onValueChange = onSearchChange,
            modifier = Modifier
                .padding(12.dp)
                .semantics { testTag = "finance_search_input" }
        )
    }
}

@Composable
private fun FinanceListCard(
    item: FinanceReportDetail,
    isAdmin: Boolean,
    onShowDetail: (FinanceReportDetail) -> Unit,
    onShowEdit: (FinanceReportDetail) -> Unit,
    onToggleVisibility: (FinanceReportDetail) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onShowDetail(item) }),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
            ) {
                Spacer(modifier = Modifier.height(96.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.description.isNotBlank()) {
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (isAdmin) {
                        FinanceVisibilityPill(visible = item.isVisibleToJemaat)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.form_amount),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                            Text(
                                text = formatCurrency(item.amount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        JemaatPill(
                            text = item.periodLabel,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (isAdmin) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onShowDetail(item) },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { testTag = "finance_detail_${item.id}" },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = stringResource(id = R.string.action_detail),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.action_detail),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        FilledTonalButton(
                            onClick = { onShowEdit(item) },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { testTag = "finance_edit_${item.id}" },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(id = R.string.action_edit),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.action_edit),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onToggleVisibility(item) },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { testTag = "finance_toggle_${item.id}" },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (item.isVisibleToJemaat) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.action_toggle_visibility),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Button(
                            onClick = { onDelete(item.id) },
                            modifier = Modifier
                                .semantics { testTag = "finance_delete_${item.id}" },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(id = R.string.action_delete),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    FilledTonalButton(
                        onClick = { onShowDetail(item) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = "finance_detail_${item.id}" },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.action_detail),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceVisibilityPill(visible: Boolean) {
    val containerColor = if (visible) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (visible) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(
                    id = if (visible) R.string.visibility_visible else R.string.visibility_hidden
                ),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun FinanceReportDetail.matchesFinanceQuery(query: String): Boolean {
    val keyword = query.trim().lowercase()
    if (keyword.isBlank()) return true
    return title.lowercase().contains(keyword) ||
        description.lowercase().contains(keyword) ||
        periodLabel.lowercase().contains(keyword) ||
        sectorName.lowercase().contains(keyword)
}

@Composable
private fun FinanceDetailDialog(
    item: FinanceReportDetail,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatCurrency(item.amount),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FinanceDetailRow(
                    icon = Icons.Filled.CalendarMonth,
                    label = stringResource(id = R.string.form_period),
                    value = item.periodLabel
                )
                FinanceDetailRow(
                    icon = if (item.isVisibleToJemaat) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    },
                    label = stringResource(id = R.string.form_visibility),
                    value = stringResource(
                        id = if (item.isVisibleToJemaat) {
                            R.string.visibility_visible
                        } else {
                            R.string.visibility_hidden
                        }
                    )
                )
                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text(text = stringResource(id = R.string.action_close))
            }
        }
    )
}

@Composable
private fun FinanceDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value.ifBlank { "-" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
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
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(
                    id = if (initial == null) R.string.dialog_add_finance else R.string.dialog_edit_finance
                ),
                fontWeight = FontWeight.Bold
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
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
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
                enabled = title.isNotBlank() && period.isNotBlank() && amount.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
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

private val previewFinance = FinanceReportDetail(
    id = "f-1",
    title = "Kas Partangiangan Mei",
    description = "Pemasukan persembahan dan pengeluaran konsumsi selama Mei 2026.",
    periodLabel = "Mei 2026",
    amount = 2_250_000L,
    isVisibleToJemaat = true,
    sectorId = "s-1",
    sectorName = "Sektor 1 HKBP Kedaton"
)

private val previewFinanceHidden = previewFinance.copy(
    id = "f-2",
    title = "Dana Diakonia Internal",
    description = "Cadangan bantuan internal keluarga sektor yang membutuhkan.",
    periodLabel = "Triwulan II 2026",
    amount = 1_500_000L,
    isVisibleToJemaat = false
)

@Preview(name = "FinanceListCard - Jemaat", showBackground = true, widthDp = 412)
@Composable
private fun FinanceListCardJemaatPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FinanceListCard(
                item = previewFinance,
                isAdmin = false,
                onShowDetail = {},
                onShowEdit = {},
                onToggleVisibility = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "FinanceListCard - Admin Visible", showBackground = true, widthDp = 412)
@Composable
private fun FinanceListCardAdminVisiblePreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FinanceListCard(
                item = previewFinance,
                isAdmin = true,
                onShowDetail = {},
                onShowEdit = {},
                onToggleVisibility = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "FinanceListCard - Admin Hidden", showBackground = true, widthDp = 412)
@Composable
private fun FinanceListCardAdminHiddenPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FinanceListCard(
                item = previewFinanceHidden,
                isAdmin = true,
                onShowDetail = {},
                onShowEdit = {},
                onToggleVisibility = {},
                onDelete = {}
            )
        }
    }
}

@Preview(
    name = "FinanceListCard - Admin Dark",
    showBackground = true,
    widthDp = 412,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FinanceListCardAdminDarkPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FinanceListCard(
                item = previewFinance,
                isAdmin = true,
                onShowDetail = {},
                onShowEdit = {},
                onToggleVisibility = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "FinanceListHeader - Jemaat", showBackground = true, widthDp = 412)
@Composable
private fun FinanceListHeaderJemaatPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FinanceListHeader(
                title = "Laporan Keuangan",
                countLabel = "3 laporan terbuka",
                totalAmount = 4_625_000L,
                isAdmin = false
            )
        }
    }
}

@Preview(name = "FinanceListHeader - Admin", showBackground = true, widthDp = 412)
@Composable
private fun FinanceListHeaderAdminPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FinanceListHeader(
                title = "Kelola Keuangan",
                countLabel = "Menampilkan 3 dari 8",
                totalAmount = 12_750_000L,
                isAdmin = true
            )
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
