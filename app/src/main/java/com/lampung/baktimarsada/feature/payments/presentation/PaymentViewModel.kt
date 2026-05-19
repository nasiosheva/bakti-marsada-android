package com.lampung.baktimarsada.feature.payments.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.repository.AuthRepository
import com.lampung.baktimarsada.repository.MemberRepository
import com.lampung.baktimarsada.repository.PaymentObligationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
        onEvent(PaymentEvent.Refresh)
    }

    fun onEvent(event: PaymentEvent) {
        when (event) {
            PaymentEvent.Refresh -> refresh()
            is PaymentEvent.Save -> save(event.item)
            is PaymentEvent.Delete -> delete(event.id)
        }
    }

    private fun refresh() {
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

    private fun save(item: PaymentObligationDetail) {
        viewModelScope.launch(dispatcherProvider.io) {
            val session = authRepository.getCurrentSession() ?: return@launch
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.save(item, session.sectorContext)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, errorMessage = null) }
                is AppResult.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    private fun delete(id: String) {
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
