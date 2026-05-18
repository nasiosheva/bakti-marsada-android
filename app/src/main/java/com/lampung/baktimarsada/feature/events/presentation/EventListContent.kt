package com.lampung.baktimarsada.feature.events.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.ui.component.BaktiEmptyState
import com.lampung.baktimarsada.ui.component.BaktiErrorState
import com.lampung.baktimarsada.ui.component.BaktiLoadingState
import com.lampung.baktimarsada.ui.component.BaktiPullToRefreshBox
import com.lampung.baktimarsada.ui.component.BaktiScrollableStateView
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import com.lampung.baktimarsada.ui.component.JemaatPill
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun EventContent(
    isAdmin: Boolean,
    state: EventUiState,
    onRefresh: () -> Unit,
    onDelete: (String) -> Unit,
    onShowCreate: () -> Unit,
    onShowDetail: (EventDetail) -> Unit,
    onShowEdit: (EventDetail) -> Unit,
    bottomContentPadding: Dp = 0.dp
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var visibleCount by rememberSaveable { mutableStateOf(AppConstants.DEFAULT_LIST_PAGE_SIZE) }
    val listUi = rememberEventListUiState(
        isAdmin = isAdmin,
        state = state,
        searchQuery = searchQuery,
        visibleCount = visibleCount
    )
    val listContentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 16.dp + bottomContentPadding)
    val fabBottomPadding = 16.dp + bottomContentPadding

    BaktiPullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.20f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
        ) {
            EventContentBody(
                isAdmin = isAdmin,
                state = state,
                listUi = listUi,
                searchQuery = searchQuery,
                onSearchChange = {
                    searchQuery = it
                    visibleCount = AppConstants.DEFAULT_LIST_PAGE_SIZE
                },
                onLoadMore = { visibleCount += AppConstants.DEFAULT_LIST_PAGE_SIZE },
                onRefresh = onRefresh,
                onDelete = onDelete,
                onShowDetail = onShowDetail,
                onShowEdit = onShowEdit,
                listContentPadding = listContentPadding
            )
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = fabBottomPadding)
                        .semantics { testTag = "event_add_fab" },
                    icon = { Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.action_add_event)) },
                    text = { Text(text = stringResource(id = R.string.action_add_event)) },
                    onClick = onShowCreate
                )
            }
        }
    }
}

@Composable
private fun rememberEventListUiState(
    isAdmin: Boolean,
    state: EventUiState,
    searchQuery: String,
    visibleCount: Int
): EventListUiState {
    val filteredItems = state.items.filter { it.matchesEventQuery(searchQuery) }
    val displayItems = filteredItems.take(visibleCount)
    val canLoadMore = displayItems.size < filteredItems.size
    val title = stringResource(id = if (isAdmin) R.string.event_title_admin else R.string.event_title_jemaat)
    val countLabel = if (isAdmin) {
        stringResource(id = R.string.pagination_summary, displayItems.size, filteredItems.size)
    } else {
        stringResource(id = R.string.jemaat_count_events, filteredItems.size)
    }
    return EventListUiState(filteredItems, displayItems, canLoadMore, title, countLabel)
}

@Composable
private fun EventContentBody(
    isAdmin: Boolean,
    state: EventUiState,
    listUi: EventListUiState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onDelete: (String) -> Unit,
    onShowDetail: (EventDetail) -> Unit,
    onShowEdit: (EventDetail) -> Unit,
    listContentPadding: PaddingValues
) {
    when {
        state.isLoading && state.items.isEmpty() -> BaktiScrollableStateView { BaktiLoadingState() }
        state.errorMessage != null && state.items.isEmpty() -> {
            BaktiScrollableStateView { BaktiErrorState(message = state.errorMessage, onRetry = onRefresh) }
        }
        else -> {
            EventListContainer(
                isAdmin = isAdmin,
                listUi = listUi,
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                state = state,
                listContentPadding = listContentPadding,
                onDelete = onDelete,
                onShowDetail = onShowDetail,
                onShowEdit = onShowEdit,
                onLoadMore = onLoadMore
            )
        }
    }
}

@Composable
private fun EventListContainer(
    isAdmin: Boolean,
    listUi: EventListUiState,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    state: EventUiState,
    listContentPadding: PaddingValues,
    onDelete: (String) -> Unit,
    onShowDetail: (EventDetail) -> Unit,
    onShowEdit: (EventDetail) -> Unit,
    onLoadMore: () -> Unit
) {
    val groupedSections = if (!isAdmin) rememberJemaatEventSections(listUi.displayItems) else emptyList()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = listContentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            EventListHeader(
                title = listUi.title,
                countLabel = listUi.countLabel,
                isAdmin = isAdmin,
                modifier = Modifier.semantics { testTag = "event_title" }
            )
        }
        item { EventSearchPanel(searchQuery = searchQuery, onSearchChange = onSearchChange) }
        state.errorMessage?.let { message -> item { BaktiSectionMessage(message = message) } }
        if (listUi.filteredItems.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                    BaktiEmptyState(message = stringResource(id = R.string.event_empty))
                }
            }
            return@LazyColumn
        }
        if (!isAdmin) {
            groupedSections.forEach { section ->
                item {
                    EventSectionHeader(
                        title = section.title,
                        count = section.items.size
                    )
                }
                items(section.items, key = { it.id }) { item ->
                    EventListCard(
                        item = item,
                        isAdmin = false,
                        onShowDetail = onShowDetail,
                        onShowEdit = onShowEdit,
                        onDelete = onDelete,
                        modifier = Modifier.semantics { testTag = "event_item_${item.id}" }
                    )
                }
            }
        } else {
            items(listUi.displayItems, key = { it.id }) { item ->
                EventListCard(
                    item = item,
                    isAdmin = true,
                    onShowDetail = onShowDetail,
                    onShowEdit = onShowEdit,
                    onDelete = onDelete,
                    modifier = Modifier.semantics { testTag = "event_item_${item.id}" }
                )
            }
        }
        if (listUi.canLoadMore) {
            item {
                OutlinedButton(onClick = onLoadMore, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.action_load_more))
                }
            }
        }
    }
}

private data class EventListUiState(
    val filteredItems: List<EventDetail>,
    val displayItems: List<EventDetail>,
    val canLoadMore: Boolean,
    val title: String,
    val countLabel: String
)

private data class EventSection(
    val title: String,
    val items: List<EventDetail>
)

@Composable
private fun rememberJemaatEventSections(items: List<EventDetail>): List<EventSection> {
    val weekTitle = stringResource(id = R.string.event_section_this_week)
    val monthTitle = stringResource(id = R.string.event_section_this_month)
    val moreThanMonthTitle = stringResource(id = R.string.event_section_above_one_month)
    return buildEventSections(
        items = items,
        weekTitle = weekTitle,
        monthTitle = monthTitle,
        moreThanMonthTitle = moreThanMonthTitle
    )
}

private fun buildEventSections(
    items: List<EventDetail>,
    weekTitle: String,
    monthTitle: String,
    moreThanMonthTitle: String
): List<EventSection> {
    val today = Calendar.getInstance().startOfDay()
    val inAWeek = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 7) }
    val inAMonth = (today.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
    val weekItems = mutableListOf<EventDetail>()
    val monthItems = mutableListOf<EventDetail>()
    val moreThanMonthItems = mutableListOf<EventDetail>()

    items.forEach { item ->
        val eventDate = item.scheduledAt.toEventCalendar() ?: run {
            moreThanMonthItems += item
            return@forEach
        }
        when {
            !eventDate.before(today) && !eventDate.after(inAWeek) -> weekItems += item
            eventDate.after(inAWeek) && !eventDate.after(inAMonth) -> monthItems += item
            else -> moreThanMonthItems += item
        }
    }

    return listOf(
        EventSection(weekTitle, weekItems),
        EventSection(monthTitle, monthItems),
        EventSection(moreThanMonthTitle, moreThanMonthItems)
    ).filter { it.items.isNotEmpty() }
}

private fun String.toEventCalendar(): Calendar? {
    val datePortion = trim().take(AppConstants.DATE_FORMAT_ISO_LOCAL_DATE.length)
    val parsedDate = runCatching {
        SimpleDateFormat(AppConstants.DATE_FORMAT_ISO_LOCAL_DATE, Locale.US).parse(datePortion)
    }.getOrNull() ?: return null
    return Calendar.getInstance().apply { time = parsedDate }.startOfDay()
}

private fun Calendar.startOfDay(): Calendar {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return this
}

@Composable
private fun EventListHeader(
    title: String,
    countLabel: String,
    isAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            JemaatPill(
                text = if (isAdmin) stringResource(id = R.string.profile_role_admin) else stringResource(id = R.string.profile_role_jemaat),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
            Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(text = countLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f))
        }
    }
}

@Composable
private fun EventSectionHeader(
    title: String,
    count: Int
) {
    Text(
        text = stringResource(id = R.string.event_section_with_count, title, count),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun EventSearchPanel(searchQuery: String, onSearchChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        BaktiTextInput(
            value = searchQuery,
            label = stringResource(id = R.string.search_events),
            onValueChange = onSearchChange,
            modifier = Modifier.padding(12.dp).semantics { testTag = "event_search_input" }
        )
    }
}

@Composable
private fun EventListCard(
    item: EventDetail,
    isAdmin: Boolean,
    onShowDetail: (EventDetail) -> Unit,
    onShowEdit: (EventDetail) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(text = item.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (isAdmin) {
                    JemaatPill(text = item.sectorName, containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JemaatPill(text = item.scheduledAt, modifier = Modifier.weight(1f))
                JemaatPill(text = item.location, modifier = Modifier.weight(1f))
            }
            if (isAdmin) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onShowDetail(item) }, modifier = Modifier.weight(1f).semantics { testTag = "event_detail_${item.id}" }) {
                            Icon(imageVector = Icons.Filled.Info, contentDescription = stringResource(id = R.string.action_detail))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(id = R.string.action_detail))
                        }
                        OutlinedButton(onClick = { onShowEdit(item) }, modifier = Modifier.weight(1f).semantics { testTag = "event_edit_${item.id}" }) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = stringResource(id = R.string.action_edit))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(id = R.string.action_edit))
                        }
                    }
                    Button(onClick = { onDelete(item.id) }, modifier = Modifier.fillMaxWidth().semantics { testTag = "event_delete_${item.id}" }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = stringResource(id = R.string.action_delete))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.action_delete))
                    }
                }
            } else {
                OutlinedButton(onClick = { onShowDetail(item) }, modifier = Modifier.fillMaxWidth().semantics { testTag = "event_detail_${item.id}" }) {
                    Text(text = stringResource(id = R.string.action_detail))
                }
            }
        }
    }
}

internal fun EventDetail.matchesEventQuery(query: String): Boolean {
    val keyword = query.trim().lowercase()
    if (keyword.isBlank()) return true
    return title.lowercase().contains(keyword) ||
        description.lowercase().contains(keyword) ||
        location.lowercase().contains(keyword) ||
        scheduledAt.lowercase().contains(keyword) ||
        sectorName.lowercase().contains(keyword)
}

// created by Mories Deo Hutapea, S.E.,S.Kom
