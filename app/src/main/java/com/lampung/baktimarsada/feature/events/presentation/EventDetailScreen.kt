package com.lampung.baktimarsada.feature.events.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.EventProgramItem
import com.lampung.baktimarsada.domain.model.ProgramItemType
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.TenantContext
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.feature.app.navigation.AppRoutes
import com.lampung.baktimarsada.repository.AuthRepository
import com.lampung.baktimarsada.repository.EventRepository
import com.lampung.baktimarsada.ui.component.BaktiBottomSheet
import com.lampung.baktimarsada.ui.component.BaktiEmptyState
import com.lampung.baktimarsada.ui.component.BaktiErrorState
import com.lampung.baktimarsada.ui.component.BaktiLoadingState
import com.lampung.baktimarsada.ui.component.BaktiPullToRefreshBox
import com.lampung.baktimarsada.ui.component.BaktiScrollableStateView
import com.lampung.baktimarsada.ui.component.BaktiToolbar
import com.lampung.baktimarsada.ui.component.JemaatPill
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun EventDetailRoute(
    eventId: String,
    isAdmin: Boolean,
    session: SessionState,
    onBack: () -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val errorMessage = state.errorMessage

    Scaffold(
        topBar = {
            BaktiToolbar(
                title = stringResource(id = R.string.event_detail_title),
                subtitle = session.sectorContext.sectorName,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        BaktiPullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading && state.item == null -> {
                    BaktiScrollableStateView { BaktiLoadingState() }
                }
                errorMessage != null && state.item == null -> {
                    BaktiScrollableStateView {
                        BaktiErrorState(
                            message = errorMessage,
                            onRetry = viewModel::refresh
                        )
                    }
                }
                state.item == null -> {
                    BaktiScrollableStateView {
                        BaktiEmptyState(message = stringResource(id = R.string.event_empty))
                    }
                }
                else -> {
                    EventDetailContent(
                        item = state.item,
                        isAdmin = isAdmin,
                        session = session
                    )
                }
            }
        }
    }
}

@Composable
fun EventDetailContent(
    item: EventDetail?,
    isAdmin: Boolean,
    session: SessionState
) {
    val event = item ?: return
    val programItems = event.programItems.sortedBy { it.orderIndex }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showScrollToTopButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    var selectedItem by remember { mutableStateOf<Pair<Int, EventProgramItem>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
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
                ),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                EventDetailHero(
                    event = event,
                    sectorName = session.sectorContext.sectorName,
                    isAdmin = isAdmin
                )
            }
            item {
                EventDetailInfoCard(event = event, isAdmin = isAdmin)
            }
            item {
                EventProgramHeader(count = programItems.size)
            }
            if (programItems.isEmpty()) {
                item {
                    EventLegacyDescriptionCard(description = event.description)
                }
            } else {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        programItems.forEachIndexed { index, programItem ->
                            EventTimelineRow(
                                index = index,
                                isLast = index == programItems.lastIndex,
                                programItem = programItem,
                                onClick = { selectedItem = index to programItem }
                            )
                        }
                    }
                }
            }
        }

        if (showScrollToTopButton) {
            SmallFloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = {
                    scope.launch { listState.animateScrollToItem(0) }
                },
                content = {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = stringResource(id = R.string.action_scroll_to_top)
                    )
                }
            )
        }
    }

    selectedItem?.let { (index, programItem) ->
        BaktiBottomSheet(
            onDismissRequest = { selectedItem = null },
            title = programItem.title.ifBlank { stringResource(id = R.string.program_section_title) }
        ) {
            EventProgramItemCard(
                index = index,
                programItem = programItem,
                showIndexBadge = true
            )
        }
    }
}

@Composable
private fun EventDetailHero(
    event: EventDetail,
    sectorName: String,
    isAdmin: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
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
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    JemaatPill(
                        text = if (isAdmin) {
                            stringResource(id = R.string.profile_role_admin)
                        } else {
                            stringResource(id = R.string.profile_role_jemaat)
                        },
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                    JemaatPill(
                        text = sectorName,
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if (event.description.isNotBlank()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EventDetailInfoCard(
    event: EventDetail,
    isAdmin: Boolean
) {
    val context = LocalContext.current
    val locationUrl = remember(event.location) { event.location.extractFirstUrl() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EventDetailInfoRow(
                icon = Icons.Filled.CalendarMonth,
                label = stringResource(id = R.string.form_schedule),
                value = event.scheduledAt
            )
            EventDetailInfoRow(
                icon = Icons.Filled.LocationOn,
                label = stringResource(id = R.string.form_location),
                value = event.location,
                trailing = {
                    if (locationUrl != null) {
                        IconButton(
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(locationUrl))
                                    )
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = stringResource(id = R.string.action_open_maps),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
            if (isAdmin) {
                EventDetailInfoRow(
                    icon = Icons.Filled.Groups,
                    label = stringResource(id = R.string.form_role_sector),
                    value = event.sectorName
                )
            }
        }
    }
}

@Composable
private fun EventDetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
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
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        trailing?.invoke()
    }
}

@Composable
private fun EventProgramHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(id = R.string.program_section_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.pagination_summary, count, count),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Text(
                text = "$count",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EventTimelineRow(
    index: Int,
    isLast: Boolean,
    programItem: EventProgramItem,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EventTimelineRail(
            index = index,
            isLast = isLast,
            modifier = Modifier.fillMaxHeight()
        )
        EventProgramItemCard(
            index = index,
            programItem = programItem,
            showIndexBadge = false,
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 14.dp)
        )
    }
}

@Composable
private fun EventTimelineRail(
    index: Int,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.width(36.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(2.dp)
                .fillMaxHeight()
                .background(
                    if (isLast) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    }
                )
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(36.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            border = BorderStroke(3.dp, MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EventLegacyDescriptionCard(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = description,
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun String.extractFirstUrl(): String? {
    return Regex("""https?://\S+""")
        .find(this)
        ?.value
        ?.trimEnd('.', ',', ';', ')', ']')
}

data class EventDetailUiState(
    val item: EventDetail? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EventRepository,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val eventId: String = savedStateHandle.get<String>(AppRoutes.EVENT_ID_ARG).orEmpty()

    private val _state = MutableStateFlow(EventDetailUiState())
    val state: StateFlow<EventDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            repository.observeEvents().collect { items ->
                _state.update {
                    it.copy(
                        item = items.firstOrNull { item -> item.id == eventId },
                        isLoading = false
                    )
                }
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
}

private val previewSession = SessionState(
    authToken = "token-preview",
    userId = "u-preview",
    displayName = "Demo User",
    role = UserRole.JEMAAT,
    tenantContext = TenantContext(
        tenantId = "t-hkbp-kedaton",
        tenantName = "HKBP",
        subTenantId = "st-kedaton",
        subTenantName = "HKBP Kedaton"
    ),
    sectorContext = SectorContext(
        sectorId = "s-1",
        sectorName = "Sektor 1"
    )
)

private val previewEventWithProgram = EventDetail(
    id = "e-1",
    title = "Partangiangan Sektor",
    description = "Ibadah sektor mingguan.",
    scheduledAt = "Minggu, 17 Mei 2026 19:00",
    location = "Rumah Keluarga Simanjuntak",
    sectorId = "s-1",
    sectorName = "Sektor 1",
    programItems = listOf(
        EventProgramItem(
            id = "p-1",
            eventId = "e-1",
            orderIndex = 0,
            title = "Pembukaan",
            content = "Salam dan pengantar singkat.",
            leader = "Liturgis",
            type = ProgramItemType.OPENING
        ),
        EventProgramItem(
            id = "p-2",
            eventId = "e-1",
            orderIndex = 1,
            title = "Pembacaan Firman",
            content = "",
            leader = "Pemimpin Ibadah",
            type = ProgramItemType.SCRIPTURE,
            scriptureReference = "Mazmur 23:1-4",
            scriptureText = "Tuhan adalah gembalaku, takkan kekurangan aku."
        ),
        EventProgramItem(
            id = "p-3",
            eventId = "e-1",
            orderIndex = 2,
            title = "Pengumuman",
            content = "Latihan koor sektor hari Rabu.",
            leader = "",
            type = ProgramItemType.ANNOUNCEMENT
        )
    )
)

private val previewEventWithoutProgram = EventDetail(
    id = "e-2",
    title = "Partangiangan Keluarga",
    description = "Ibadah keluarga dan doa syafaat.",
    scheduledAt = "Rabu, 20 Mei 2026 20:00",
    location = "Rumah Keluarga Siregar",
    sectorId = "s-1",
    sectorName = "Sektor 1",
    programItems = emptyList()
)

@Preview(name = "Event Detail - Jemaat")
@Composable
private fun EventDetailContentJemaatPreview() {
    BaktiMarsadaTheme {
        EventDetailContent(
            item = previewEventWithProgram,
            isAdmin = false,
            session = previewSession
        )
    }
}

@Preview(name = "Event Detail - Admin")
@Composable
private fun EventDetailContentAdminPreview() {
    BaktiMarsadaTheme {
        EventDetailContent(
            item = previewEventWithProgram,
            isAdmin = true,
            session = previewSession.copy(role = UserRole.ADMIN)
        )
    }
}

@Preview(name = "Event Detail - Legacy Description")
@Composable
private fun EventDetailContentLegacyPreview() {
    BaktiMarsadaTheme {
        EventDetailContent(
            item = previewEventWithoutProgram,
            isAdmin = false,
            session = previewSession
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
