package com.lampung.baktimarsada.feature.events.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.background,
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

private data class EventListUiState(
    val filteredItems: List<EventDetail>,
    val displayItems: List<EventDetail>,
    val canLoadMore: Boolean,
    val title: String,
    val countLabel: String
)

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
    val gatheringLabel = com.lampung.baktimarsada.ui.tenant.BaktiTerminologies.current.gatheringLabel
    val title = stringResource(
        id = if (isAdmin) R.string.event_title_admin else R.string.event_title_jemaat,
        gatheringLabel
    )
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
                OutlinedButton(
                    onClick = onLoadMore,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = stringResource(id = R.string.action_load_more))
                }
            }
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
