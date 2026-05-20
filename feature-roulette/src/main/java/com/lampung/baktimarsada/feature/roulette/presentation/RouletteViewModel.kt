package com.lampung.baktimarsada.feature.roulette.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.resources.StringProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.feature.roulette.R
import com.lampung.baktimarsada.feature.roulette.data.RouletteImportDataSource
import com.lampung.baktimarsada.feature.roulette.data.RouletteLocalDataSource
import com.lampung.baktimarsada.feature.roulette.model.RouletteHistoryItem
import com.lampung.baktimarsada.feature.roulette.model.RouletteNameSource
import com.lampung.baktimarsada.feature.roulette.model.RoulettePendingSpin
import com.lampung.baktimarsada.feature.roulette.model.RouletteUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RouletteViewModel @Inject constructor(
    private val localStore: RouletteLocalDataSource,
    private val importDataSource: RouletteImportDataSource,
    private val stringProvider: StringProvider,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {
    private val _state = MutableStateFlow(RouletteUiState())
    val state: StateFlow<RouletteUiState> = _state.asStateFlow()

    init {
        observeLocalState()
        observeSession()
        observeMembers()
    }

    fun requestSpin(currentRotationDegrees: Float): Boolean {
        val names = _state.value.names
        if (names.isEmpty()) {
            _state.update {
                it.copy(message = stringProvider.get(R.string.roulette_spin_requires_names))
            }
            return false
        }
        val selectedIndex = Random.nextInt(names.size)
        val selectedName = names[selectedIndex]
        val sweepAngle = 360f / names.size
        val selectedCenterOffset = (selectedIndex * sweepAngle) + (sweepAngle / 2f)
        val targetNormalizedRotation = (360f - selectedCenterOffset).normalizeDegrees()
        val currentNormalizedRotation = currentRotationDegrees.normalizeDegrees()
        val deltaRotation = (targetNormalizedRotation - currentNormalizedRotation).normalizeDegrees()
        val totalRotation = (Random.nextInt(MIN_FULL_TURNS, MAX_FULL_TURNS + 1) * 360f) + deltaRotation
        _state.update {
            it.copy(
                pendingSpin = RoulettePendingSpin(
                    requestId = System.currentTimeMillis(),
                    selectedIndex = selectedIndex,
                    selectedName = selectedName,
                    deltaRotationDegrees = totalRotation
                )
            )
        }
        return true
    }

    fun confirmSpin(requestId: Long) {
        val pendingSpin = _state.value.pendingSpin ?: return
        if (pendingSpin.requestId != requestId) return
        viewModelScope.launch(dispatcherProvider.io) {
            localStore.appendHistory(pendingSpin.selectedName)
            _state.update {
                it.copy(
                    pendingSpin = null,
                    latestWinner = RouletteHistoryItem(
                        id = requestId.toString(),
                        winnerName = pendingSpin.selectedName,
                        drawnAtMillis = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun dismissWinner() {
        _state.update { it.copy(latestWinner = null) }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    fun saveManualNames(rawValue: String) {
        val parsedNames = rawValue
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()
        viewModelScope.launch(dispatcherProvider.io) {
            localStore.replaceNames(parsedNames, RouletteNameSource.MANUAL)
            _state.update {
                it.copy(message = stringProvider.get(R.string.roulette_names_saved))
            }
        }
    }

    fun consumeIntentNames(names: List<String>) {
        val parsedNames = names
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        if (parsedNames.isEmpty()) return
        viewModelScope.launch(dispatcherProvider.io) {
            localStore.replaceNames(parsedNames, RouletteNameSource.MANUAL)
            _state.update {
                it.copy(message = stringProvider.get(R.string.roulette_names_saved))
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch(dispatcherProvider.io) {
            localStore.clearHistory()
            _state.update {
                it.copy(message = stringProvider.get(R.string.roulette_history_cleared))
            }
        }
    }

    fun prepareImportMembers() {
        if (_state.value.session == null) return
        refreshMembers()
    }

    fun loginForImport(
        identifier: String,
        password: String
    ) {
        if (identifier.isBlank()) {
            _state.update {
                it.copy(message = stringProvider.get(R.string.roulette_login_identifier_required))
            }
            return
        }
        if (password.isBlank()) {
            _state.update {
                it.copy(message = stringProvider.get(R.string.roulette_login_password_required))
            }
            return
        }
        viewModelScope.launch(dispatcherProvider.io) {
            _state.update { it.copy(isLoggingIn = true, message = null) }
            when (val result = importDataSource.login(identifier.trim(), password)) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoggingIn = false,
                            message = stringProvider.get(R.string.roulette_login_success)
                        )
                    }
                    refreshMembers()
                }
                is AppResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoggingIn = false,
                            message = result.message
                        )
                    }
                }
            }
        }
    }

    fun importSelectedMembers(selectedIds: Set<String>) {
        if (selectedIds.isEmpty()) {
            _state.update {
                it.copy(message = stringProvider.get(R.string.roulette_import_no_selection))
            }
            return
        }
        val selectedNames = _state.value.members
            .filter { it.id in selectedIds }
            .map { it.fullName }
        viewModelScope.launch(dispatcherProvider.io) {
            localStore.replaceNames(selectedNames, RouletteNameSource.IMPORT)
            _state.update {
                it.copy(message = stringProvider.get(R.string.roulette_import_success))
            }
        }
    }

    private fun observeLocalState() {
        viewModelScope.launch {
            localStore.snapshot.collect { snapshot ->
                _state.update {
                    it.copy(
                        names = snapshot.names,
                        history = snapshot.history,
                        source = snapshot.source,
                        lastUpdatedMillis = snapshot.lastUpdatedMillis
                    )
                }
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            importDataSource.observeSession().collect { session ->
                _state.update { current ->
                    current.copy(
                        session = session,
                        members = if (session == null) emptyList() else current.members
                    )
                }
            }
        }
    }

    private fun observeMembers() {
        viewModelScope.launch {
            importDataSource.observeMembers().collect { members ->
                if (_state.value.session != null) {
                    _state.update { it.copy(members = members) }
                }
            }
        }
    }

    private fun refreshMembers() {
        viewModelScope.launch(dispatcherProvider.io) {
            _state.update { it.copy(isLoadingMembers = true, message = null) }
            when (val result = importDataSource.refreshMembers()) {
                is AppResult.Success -> {
                    _state.update { it.copy(isLoadingMembers = false) }
                }
                is AppResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoadingMembers = false,
                            message = result.message
                        )
                    }
                }
            }
        }
    }

    private fun Float.normalizeDegrees(): Float {
        val normalized = this % 360f
        return if (normalized < 0f) normalized + 360f else normalized
    }

    private companion object {
        const val MIN_FULL_TURNS = 4
        const val MAX_FULL_TURNS = 6
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
