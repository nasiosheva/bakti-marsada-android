package com.lampung.baktimarsada.feature.events.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.lampung.baktimarsada.domain.model.TenantContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.domain.repository.AuthRepository
import com.lampung.baktimarsada.domain.repository.EventRepository
import com.lampung.baktimarsada.feature.app.navigation.AppRoutes
import com.lampung.baktimarsada.ui.component.BaktiEmptyState
import com.lampung.baktimarsada.ui.component.BaktiErrorState
import com.lampung.baktimarsada.ui.component.BaktiLoadingState
import com.lampung.baktimarsada.ui.component.BaktiPullToRefreshBox
import com.lampung.baktimarsada.ui.component.BaktiScrollableStateView
import com.lampung.baktimarsada.ui.component.BaktiToolbar
import com.lampung.baktimarsada.ui.component.BaktiValueRow
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = session.sectorContext.sectorName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JemaatPill(text = event.scheduledAt)
                JemaatPill(text = event.location)
            }
        }
        item {
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BaktiValueRow(
                        label = stringResource(id = R.string.form_schedule),
                        value = event.scheduledAt
                    )
                    BaktiValueRow(
                        label = stringResource(id = R.string.form_location),
                        value = event.location
                    )
                    if (isAdmin) {
                        BaktiValueRow(
                            label = stringResource(id = R.string.form_role_sector),
                            value = event.sectorName
                        )
                    }
                    BaktiValueRow(
                        label = stringResource(id = R.string.form_description),
                        value = event.description
                    )
                }
            }
        }
        item {
            Text(
                text = stringResource(id = R.string.program_section_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (event.programItems.isEmpty()) {
            item {
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = event.description,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(event.programItems.sortedBy { it.orderIndex }.size) { index ->
                val programItem = event.programItems.sortedBy { it.orderIndex }[index]
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            JemaatPill(text = "${index + 1}")
                            JemaatPill(text = programItem.type.name)
                        }
                        if (programItem.title.isNotBlank()) {
                            Text(
                                text = programItem.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (programItem.leader.isNotBlank()) {
                            Text(
                                text = programItem.leader,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        ProgramItemBody(programItem = programItem)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgramItemBody(programItem: EventProgramItem) {
    val bodyText = when (programItem.type) {
        ProgramItemType.SCRIPTURE -> listOf(
            programItem.scriptureReference,
            programItem.scriptureText.ifBlank { programItem.content }
        ).filter { it.isNotBlank() }.joinToString(separator = "\n")
        ProgramItemType.OFFERING -> programItem.note.ifBlank { programItem.content }
        else -> programItem.content
    }
    if (bodyText.isNotBlank()) {
        Text(
            text = bodyText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
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
