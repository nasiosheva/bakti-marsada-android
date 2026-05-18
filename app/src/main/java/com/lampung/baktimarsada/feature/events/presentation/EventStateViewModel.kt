package com.lampung.baktimarsada.feature.events.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.EventProgramItem
import com.lampung.baktimarsada.domain.model.WorshipTemplate
import com.lampung.baktimarsada.domain.model.WorshipTemplateItem
import com.lampung.baktimarsada.domain.repository.WorshipTemplateRepository
import com.lampung.baktimarsada.repository.AuthRepository
import com.lampung.baktimarsada.repository.EventRepository
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventUiState(
    val items: List<EventDetail> = emptyList(),
    val templates: List<WorshipTemplate> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val templateErrorMessage: String? = null
)

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    private val templateRepository: WorshipTemplateRepository,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _state = MutableStateFlow(EventUiState())
    val state: StateFlow<EventUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            repository.observeEvents().collect { items ->
                _state.update { it.copy(items = items, isLoading = false) }
            }
        }
        viewModelScope.launch(dispatcherProvider.io) {
            templateRepository.observeTemplates().collect { templates ->
                _state.update { it.copy(templates = templates) }
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
            when (val result = templateRepository.refresh(session.sectorContext)) {
                is AppResult.Success -> _state.update { it.copy(templateErrorMessage = null) }
                is AppResult.Error -> _state.update { it.copy(templateErrorMessage = result.message) }
            }
        }
    }

    fun save(item: EventDetail) {
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

    fun saveTemplate(template: WorshipTemplate) {
        viewModelScope.launch(dispatcherProvider.io) {
            val session = authRepository.getCurrentSession() ?: return@launch
            _state.update { it.copy(templateErrorMessage = null) }
            when (val result = templateRepository.save(template, session.sectorContext)) {
                is AppResult.Success -> _state.update { it.copy(templateErrorMessage = null) }
                is AppResult.Error -> _state.update { it.copy(templateErrorMessage = result.message) }
            }
        }
    }

    fun deleteTemplate(id: String) {
        viewModelScope.launch(dispatcherProvider.io) {
            val session = authRepository.getCurrentSession() ?: return@launch
            _state.update { it.copy(templateErrorMessage = null) }
            when (val result = templateRepository.delete(id, session.sectorContext)) {
                is AppResult.Success -> _state.update { it.copy(templateErrorMessage = null) }
                is AppResult.Error -> _state.update { it.copy(templateErrorMessage = result.message) }
            }
        }
    }
}

internal fun WorshipTemplateItem.toEventProgramItem(eventId: String, orderIndex: Int) = EventProgramItem(
    id = "",
    eventId = eventId,
    orderIndex = orderIndex,
    title = title,
    content = content,
    leader = leader,
    type = type,
    scriptureReference = scriptureReference,
    scriptureText = scriptureText,
    note = note
)

@Preview(name = "Event Content - Jemaat", showBackground = true)
@Composable
private fun EventContentJemaatPreview() {
    BaktiMarsadaTheme {
        EventContent(
            isAdmin = false,
            state = EventUiState(
                items = listOf(
                    EventDetail(
                        id = "event-1",
                        title = "Partangiangan Sektor",
                        description = "Ibadah rutin sektor HKBP Kedaton.",
                        scheduledAt = "2026-05-20",
                        location = "Rumah Keluarga Hutapea",
                        sectorId = "sector-1",
                        sectorName = "Sektor 1"
                    ),
                    EventDetail(
                        id = "event-2",
                        title = "PA Pemuda",
                        description = "Pendalaman Alkitab pemuda setiap Jumat.",
                        scheduledAt = "2026-05-22",
                        location = "Ruang Serbaguna",
                        sectorId = "sector-2",
                        sectorName = "Sektor 2"
                    )
                ),
                isLoading = false
            ),
            onRefresh = {},
            onDelete = {},
            onShowCreate = {},
            onShowDetail = {},
            onShowEdit = {}
        )
    }
}

@Preview(name = "Event Content - Admin", showBackground = true)
@Composable
private fun EventContentAdminPreview() {
    BaktiMarsadaTheme {
        EventContent(
            isAdmin = true,
            state = EventUiState(
                items = listOf(
                    EventDetail(
                        id = "event-3",
                        title = "Kebaktian Umum",
                        description = "Kebaktian Minggu pagi.",
                        scheduledAt = "2026-05-25",
                        location = "Gereja HKBP Kedaton",
                        sectorId = "sector-1",
                        sectorName = "Sektor 1"
                    )
                ),
                isLoading = false
            ),
            onRefresh = {},
            onDelete = {},
            onShowCreate = {},
            onShowDetail = {},
            onShowEdit = {}
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
