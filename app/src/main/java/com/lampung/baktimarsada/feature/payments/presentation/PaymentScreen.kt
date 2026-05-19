package com.lampung.baktimarsada.feature.payments.presentation

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.domain.model.PaymentStatus
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.ui.component.BaktiAmountInput
import com.lampung.baktimarsada.ui.component.BaktiDropdown
import com.lampung.baktimarsada.ui.component.BaktiEmptyState
import com.lampung.baktimarsada.ui.component.BaktiErrorState
import com.lampung.baktimarsada.ui.component.BaktiLoadingState
import com.lampung.baktimarsada.ui.component.BaktiMultilineInput
import com.lampung.baktimarsada.ui.component.BaktiPullToRefreshBox
import com.lampung.baktimarsada.ui.component.BaktiScrollableStateView
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme
import com.lampung.baktimarsada.ui.util.formatCurrency

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
        onRefresh = { viewModel.onEvent(PaymentEvent.Refresh) },
        onDelete = { viewModel.onEvent(PaymentEvent.Delete(it)) },
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
                viewModel.onEvent(PaymentEvent.Save(it))
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
                viewModel.onEvent(PaymentEvent.Save(it))
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
    val unpaidCount = filteredItems.count { it.status != PaymentStatus.PAID }
    val totalUnpaid = filteredItems
        .filter { it.status != PaymentStatus.PAID }
        .sumOf { it.amount }
    val title = stringResource(
        id = if (isAdmin) R.string.payment_title_admin else R.string.payment_title_jemaat
    )
    val countLabel = if (isAdmin) {
        stringResource(id = R.string.pagination_summary, displayItems.size, filteredItems.size)
    } else {
        stringResource(id = R.string.jemaat_count_payments, filteredItems.size)
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
                state.isLoading && state.items.isEmpty() -> {
                    BaktiScrollableStateView { BaktiLoadingState() }
                }
                state.errorMessage != null && state.items.isEmpty() -> {
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
                            PaymentListHeader(
                                title = title,
                                countLabel = countLabel,
                                unpaidCount = unpaidCount,
                                totalUnpaid = totalUnpaid,
                                isAdmin = isAdmin,
                                modifier = Modifier.semantics { testTag = "payment_title" }
                            )
                        }
                        item {
                            PaymentSearchPanel(
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
                                    BaktiEmptyState(message = stringResource(id = R.string.payment_empty))
                                }
                            }
                            return@LazyColumn
                        }
                        items(displayItems, key = { it.id }) { item ->
                            PaymentListCard(
                                item = item,
                                isAdmin = isAdmin,
                                onShowDetail = onShowDetail,
                                onShowEdit = onShowEdit,
                                onDelete = onDelete,
                                modifier = Modifier.semantics { testTag = "payment_item_${item.id}" }
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
                        .semantics { testTag = "payment_add_fab" },
                    onClick = onShowCreate,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(id = R.string.action_add_payment)
                        )
                    },
                    text = { Text(text = stringResource(id = R.string.action_add_payment)) }
                )
            }
        }
    }
}

@Composable
private fun PaymentListHeader(
    title: String,
    countLabel: String,
    unpaidCount: Int,
    totalUnpaid: Long,
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
                        com.lampung.baktimarsada.ui.component.JemaatPill(
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
                                imageVector = Icons.Filled.Payments,
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.payment_outstanding_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = formatCurrency(totalUnpaid),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "$unpaidCount",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentSearchPanel(
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
            label = stringResource(id = R.string.search_payments),
            onValueChange = onSearchChange,
            modifier = Modifier
                .padding(12.dp)
                .semantics { testTag = "payment_search_input" }
        )
    }
}

@Composable
private fun PaymentListCard(
    item: PaymentObligationDetail,
    isAdmin: Boolean,
    onShowDetail: (PaymentObligationDetail) -> Unit,
    onShowEdit: (PaymentObligationDetail) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = paymentStatusColor(item.status)
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
                            colors = listOf(accent, accent.copy(alpha = 0.6f))
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
                    PaymentStatusBadge(status = item.status)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatCurrency(item.amount),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        PaymentInfoLine(icon = Icons.Filled.Person, text = item.memberName)
                        PaymentInfoLine(icon = Icons.Filled.CalendarMonth, text = item.dueDate)
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
                                .semantics { testTag = "payment_detail_${item.id}" },
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
                                .semantics { testTag = "payment_edit_${item.id}" },
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
                        Button(
                            onClick = { onDelete(item.id) },
                            modifier = Modifier
                                .semantics { testTag = "payment_delete_${item.id}" },
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
                            .semantics { testTag = "payment_detail_${item.id}" },
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
private fun PaymentInfoLine(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PaymentStatusBadge(status: PaymentStatus) {
    val containerColor = when (status) {
        PaymentStatus.PAID -> MaterialTheme.colorScheme.primaryContainer
        PaymentStatus.UNPAID -> MaterialTheme.colorScheme.tertiaryContainer
        PaymentStatus.OVERDUE -> MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when (status) {
        PaymentStatus.PAID -> MaterialTheme.colorScheme.onPrimaryContainer
        PaymentStatus.UNPAID -> MaterialTheme.colorScheme.onTertiaryContainer
        PaymentStatus.OVERDUE -> MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = paymentStatusLabel(status),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun paymentStatusColor(status: PaymentStatus): Color {
    return when (status) {
        PaymentStatus.PAID -> MaterialTheme.colorScheme.primary
        PaymentStatus.UNPAID -> MaterialTheme.colorScheme.tertiary
        PaymentStatus.OVERDUE -> MaterialTheme.colorScheme.error
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
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatCurrency(item.amount),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    PaymentStatusBadge(status = item.status)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PaymentDetailRow(
                    icon = Icons.Filled.Person,
                    label = stringResource(id = R.string.form_member),
                    value = item.memberName
                )
                PaymentDetailRow(
                    icon = Icons.Filled.CalendarMonth,
                    label = stringResource(id = R.string.form_due_date),
                    value = item.dueDate
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
private fun PaymentDetailRow(
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
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(
                    id = if (initial == null) R.string.dialog_add_payment else R.string.dialog_edit_payment
                ),
                fontWeight = FontWeight.Bold
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
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
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
                enabled = memberName.isNotBlank() && title.isNotBlank() && amount.isNotBlank() && dueDate.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
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

private val previewPaymentPaid = PaymentObligationDetail(
    id = "p-1",
    memberId = "m-1",
    memberName = "P. Simanjuntak",
    title = "Iuran Sektor Mei",
    description = "Iuran bulanan sektor untuk operasional dan konsumsi partangiangan.",
    amount = 50_000L,
    dueDate = "2026-05-25",
    status = PaymentStatus.PAID,
    sectorId = "s-1",
    sectorName = "Sektor 1 HKBP Kedaton"
)

private val previewPaymentUnpaid = previewPaymentPaid.copy(
    id = "p-2",
    memberName = "S. Sihombing",
    title = "Iuran Paskah Wijk",
    amount = 100_000L,
    dueDate = "2026-05-19",
    status = PaymentStatus.UNPAID
)

private val previewPaymentOverdue = previewPaymentPaid.copy(
    id = "p-3",
    memberName = "R. Naibaho",
    title = "Partisipasi Retreat Sektor",
    amount = 250_000L,
    dueDate = "2026-05-10",
    status = PaymentStatus.OVERDUE
)

@Preview(name = "PaymentListCard - Paid", showBackground = true, widthDp = 412)
@Composable
private fun PaymentListCardPaidPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PaymentListCard(
                item = previewPaymentPaid,
                isAdmin = false,
                onShowDetail = {},
                onShowEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "PaymentListCard - Unpaid", showBackground = true, widthDp = 412)
@Composable
private fun PaymentListCardUnpaidPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PaymentListCard(
                item = previewPaymentUnpaid,
                isAdmin = false,
                onShowDetail = {},
                onShowEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "PaymentListCard - Overdue Admin", showBackground = true, widthDp = 412)
@Composable
private fun PaymentListCardOverdueAdminPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PaymentListCard(
                item = previewPaymentOverdue,
                isAdmin = true,
                onShowDetail = {},
                onShowEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview(
    name = "PaymentListCard - Admin Dark",
    showBackground = true,
    widthDp = 412,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PaymentListCardAdminDarkPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PaymentListCard(
                item = previewPaymentUnpaid,
                isAdmin = true,
                onShowDetail = {},
                onShowEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "PaymentListHeader - Jemaat", showBackground = true, widthDp = 412)
@Composable
private fun PaymentListHeaderJemaatPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PaymentListHeader(
                title = "Tagihan Sektor",
                countLabel = "3 tagihan terbuka",
                unpaidCount = 2,
                totalUnpaid = 350_000L,
                isAdmin = false
            )
        }
    }
}

@Preview(name = "PaymentListHeader - Admin", showBackground = true, widthDp = 412)
@Composable
private fun PaymentListHeaderAdminPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PaymentListHeader(
                title = "Kelola Tagihan",
                countLabel = "Menampilkan 5 dari 12",
                unpaidCount = 7,
                totalUnpaid = 1_250_000L,
                isAdmin = true
            )
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
