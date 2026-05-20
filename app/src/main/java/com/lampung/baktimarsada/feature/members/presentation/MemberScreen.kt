package com.lampung.baktimarsada.feature.members.presentation

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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
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
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.repository.AuthRepository
import com.lampung.baktimarsada.repository.MemberRepository
import com.lampung.baktimarsada.ui.component.BaktiEmptyState
import com.lampung.baktimarsada.ui.component.BaktiErrorState
import com.lampung.baktimarsada.ui.component.BaktiLoadingState
import com.lampung.baktimarsada.ui.component.BaktiPullToRefreshBox
import com.lampung.baktimarsada.ui.component.BaktiScrollableStateView
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import com.lampung.baktimarsada.ui.component.JemaatPill
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun MemberRoute(
    isAdmin: Boolean,
    session: SessionState,
    viewModel: MemberViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var detailTarget by remember { mutableStateOf<MemberDetail?>(null) }
    var editTarget by remember { mutableStateOf<MemberDetail?>(null) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }

    MemberContent(
        isAdmin = isAdmin,
        state = state,
        onRefresh = viewModel::refresh,
        onDelete = viewModel::delete,
        onShowCreate = { showCreateDialog = true },
        onShowDetail = { detailTarget = it },
        onShowEdit = { editTarget = it }
    )

    detailTarget?.let { item ->
        MemberDetailDialog(item = item, onDismiss = { detailTarget = null })
    }

    if (showCreateDialog) {
        MemberFormDialog(
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
        MemberFormDialog(
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
fun MemberContent(
    isAdmin: Boolean,
    state: MemberUiState,
    onRefresh: () -> Unit,
    onDelete: (String) -> Unit,
    onShowCreate: () -> Unit,
    onShowDetail: (MemberDetail) -> Unit,
    onShowEdit: (MemberDetail) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var visibleCount by rememberSaveable { mutableStateOf(AppConstants.DEFAULT_LIST_PAGE_SIZE) }
    val filteredItems = state.items.filter { it.matchesMemberQuery(searchQuery) }
    val displayItems = filteredItems.take(visibleCount)
    val canLoadMore = displayItems.size < filteredItems.size
    val memberLabel = com.lampung.baktimarsada.ui.tenant.BaktiTerminologies.current.memberLabel
    val title = stringResource(
        id = if (isAdmin) R.string.member_title_admin else R.string.member_title_jemaat,
        memberLabel
    )
    val countLabel = if (isAdmin) {
        stringResource(id = R.string.pagination_summary, displayItems.size, filteredItems.size)
    } else {
        stringResource(id = R.string.jemaat_count_members, filteredItems.size)
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
                            MemberListHeader(
                                title = title,
                                countLabel = countLabel,
                                isAdmin = isAdmin,
                                modifier = Modifier.semantics { testTag = "member_title" }
                            )
                        }
                        item {
                            MemberSearchPanel(
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
                                    BaktiEmptyState(message = stringResource(id = R.string.member_empty))
                                }
                            }
                            return@LazyColumn
                        }
                        items(displayItems, key = { it.id }) { item ->
                            MemberListCard(
                                item = item,
                                isAdmin = isAdmin,
                                onShowDetail = onShowDetail,
                                onShowEdit = onShowEdit,
                                onDelete = onDelete,
                                modifier = Modifier.semantics { testTag = "member_item_${item.id}" }
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
                        .semantics { testTag = "member_add_fab" },
                    onClick = onShowCreate,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(id = R.string.action_add_member)
                        )
                    },
                    text = { Text(text = stringResource(id = R.string.action_add_member)) }
                )
            }
        }
    }
}

@Composable
private fun MemberListHeader(
    title: String,
    countLabel: String,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
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
                            imageVector = Icons.Filled.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberSearchPanel(
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
            label = stringResource(id = R.string.search_members),
            onValueChange = onSearchChange,
            modifier = Modifier
                .padding(12.dp)
                .semantics { testTag = "member_search_input" }
        )
    }
}

@Composable
private fun MemberListCard(
    item: MemberDetail,
    isAdmin: Boolean,
    onShowDetail: (MemberDetail) -> Unit,
    onShowEdit: (MemberDetail) -> Unit,
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MemberAvatar(name = item.fullName)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = item.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.familyGroup.isNotBlank()) {
                            Text(
                                text = item.familyGroup,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (item.roleInSector.isNotBlank()) {
                        JemaatPill(
                            text = item.roleInSector,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                if (item.phoneNumber.isNotBlank() || item.address.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (item.phoneNumber.isNotBlank()) {
                            MemberInfoLine(icon = Icons.Filled.Phone, text = item.phoneNumber)
                        }
                        if (item.address.isNotBlank()) {
                            MemberInfoLine(icon = Icons.Filled.LocationOn, text = item.address)
                        }
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
                                .semantics { testTag = "member_detail_${item.id}" },
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
                                .semantics { testTag = "member_edit_${item.id}" },
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
                                .weight(1f)
                                .semantics { testTag = "member_delete_${item.id}" },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
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
                            .semantics { testTag = "member_detail_${item.id}" },
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
private fun MemberAvatar(name: String) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name.memberInitials(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MemberInfoLine(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun String.memberInitials(): String {
    return trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString(separator = "") { it.first().uppercase() }
        .ifBlank { "?" }
}

private fun MemberDetail.matchesMemberQuery(query: String): Boolean {
    val keyword = query.trim().lowercase()
    if (keyword.isBlank()) return true
    return fullName.lowercase().contains(keyword) ||
        familyGroup.lowercase().contains(keyword) ||
        roleInSector.lowercase().contains(keyword) ||
        address.lowercase().contains(keyword) ||
        phoneNumber.lowercase().contains(keyword)
}

@Composable
private fun MemberDetailDialog(
    item: MemberDetail,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MemberAvatar(name = item.fullName)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.roleInSector.isNotBlank()) {
                        Text(
                            text = item.roleInSector,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MemberDetailRow(
                    icon = Icons.Filled.Groups,
                    label = stringResource(id = R.string.form_family_group),
                    value = item.familyGroup
                )
                MemberDetailRow(
                    icon = Icons.Filled.Badge,
                    label = stringResource(id = R.string.form_role_sector),
                    value = item.roleInSector
                )
                MemberDetailRow(
                    icon = Icons.Filled.Phone,
                    label = stringResource(id = R.string.form_phone),
                    value = item.phoneNumber
                )
                MemberDetailRow(
                    icon = Icons.Filled.LocationOn,
                    label = stringResource(id = R.string.form_address),
                    value = item.address
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = stringResource(id = R.string.action_close))
            }
        }
    )
}

@Composable
private fun MemberDetailRow(
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
private fun MemberFormDialog(
    initial: MemberDetail?,
    session: SessionState,
    onDismiss: () -> Unit,
    onSave: (MemberDetail) -> Unit
) {
    var fullName by rememberSaveable(initial?.id) { mutableStateOf(initial?.fullName.orEmpty()) }
    var familyGroup by rememberSaveable(initial?.id) { mutableStateOf(initial?.familyGroup.orEmpty()) }
    var roleInSector by rememberSaveable(initial?.id) { mutableStateOf(initial?.roleInSector.orEmpty()) }
    var phoneNumber by rememberSaveable(initial?.id) { mutableStateOf(initial?.phoneNumber.orEmpty()) }
    var address by rememberSaveable(initial?.id) { mutableStateOf(initial?.address.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(
                    id = if (initial == null) R.string.dialog_add_member else R.string.dialog_edit_member
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
                    value = fullName,
                    label = stringResource(id = R.string.form_full_name),
                    onValueChange = { fullName = it }
                )
                BaktiTextInput(
                    value = familyGroup,
                    label = stringResource(id = R.string.form_family_group),
                    onValueChange = { familyGroup = it }
                )
                BaktiTextInput(
                    value = roleInSector,
                    label = stringResource(id = R.string.form_role_sector),
                    onValueChange = { roleInSector = it }
                )
                BaktiTextInput(
                    value = phoneNumber,
                    label = stringResource(id = R.string.form_phone),
                    onValueChange = { phoneNumber = it }
                )
                BaktiTextInput(
                    value = address,
                    label = stringResource(id = R.string.form_address),
                    onValueChange = { address = it },
                    singleLine = false
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = stringResource(id = R.string.action_cancel))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        MemberDetail(
                            id = initial?.id.orEmpty(),
                            fullName = fullName.trim(),
                            familyGroup = familyGroup.trim(),
                            phoneNumber = phoneNumber.trim(),
                            address = address.trim(),
                            roleInSector = roleInSector.trim(),
                            sectorId = session.sectorContext.sectorId,
                            sectorName = session.sectorContext.sectorName
                        )
                    )
                },
                enabled = fullName.isNotBlank() && familyGroup.isNotBlank() && roleInSector.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = stringResource(id = R.string.action_save))
            }
        }
    )
}

data class MemberUiState(
    val items: List<MemberDetail> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class MemberViewModel @Inject constructor(
    private val repository: MemberRepository,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _state = MutableStateFlow(MemberUiState())
    val state: StateFlow<MemberUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            repository.observeMembers().collect { items ->
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

    fun save(item: MemberDetail) {
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

private val previewMember = MemberDetail(
    id = "m-1",
    fullName = "Tigor Hutapea Simanjuntak",
    familyGroup = "Marga Hutapea",
    phoneNumber = "0812-3456-7890",
    address = "Jl. Kedaton No. 12, Bandar Lampung",
    roleInSector = "Penatua",
    sectorId = "s-1",
    sectorName = "Sektor 1 HKBP Kedaton"
)

private val previewMemberShort = previewMember.copy(
    id = "m-2",
    fullName = "Mariana Siregar",
    familyGroup = "Marga Siregar",
    phoneNumber = "",
    address = "",
    roleInSector = "Anggota"
)

@Preview(name = "MemberListCard - Jemaat", showBackground = true, widthDp = 412)
@Composable
private fun MemberListCardJemaatPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MemberListCard(
                item = previewMember,
                isAdmin = false,
                onShowDetail = {},
                onShowEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "MemberListCard - Admin", showBackground = true, widthDp = 412)
@Composable
private fun MemberListCardAdminPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MemberListCard(
                item = previewMember,
                isAdmin = true,
                onShowDetail = {},
                onShowEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "MemberListCard - Sparse", showBackground = true, widthDp = 412)
@Composable
private fun MemberListCardSparsePreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MemberListCard(
                item = previewMemberShort,
                isAdmin = false,
                onShowDetail = {},
                onShowEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "MemberListHeader - Jemaat", showBackground = true, widthDp = 412)
@Composable
private fun MemberListHeaderJemaatPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MemberListHeader(
                title = "Jemaat Sektor",
                countLabel = "24 anggota terdaftar",
                isAdmin = false
            )
        }
    }
}

@Preview(name = "MemberListHeader - Admin", showBackground = true, widthDp = 412)
@Composable
private fun MemberListHeaderAdminPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MemberListHeader(
                title = "Kelola Anggota",
                countLabel = "Menampilkan 10 dari 24",
                isAdmin = true
            )
        }
    }
}

@Preview(
    name = "MemberListCard - Admin Dark",
    showBackground = true,
    widthDp = 412,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun MemberListCardAdminDarkPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MemberListCard(
                item = previewMember,
                isAdmin = true,
                onShowDetail = {},
                onShowEdit = {},
                onDelete = {}
            )
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
