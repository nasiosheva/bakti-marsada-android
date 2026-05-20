package com.lampung.baktimarsada.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterChurchViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterChurchUiState())
    val state: StateFlow<RegisterChurchUiState> = _state.asStateFlow()

    fun onChurchNameChanged(value: String) {
        _state.update { it.copy(churchName = value, churchNameError = null, errorMessage = null) }
    }

    fun onDenominationChanged(value: String) {
        _state.update { it.copy(denomination = value, errorMessage = null) }
    }

    fun onTerminologyChanged(value: String) {
        _state.update { it.copy(terminologyPreset = value, errorMessage = null) }
    }

    fun onAdminFullNameChanged(value: String) {
        _state.update { it.copy(adminFullName = value, adminFullNameError = null, errorMessage = null) }
    }

    fun onAdminEmailChanged(value: String) {
        _state.update { it.copy(adminEmail = value, adminEmailError = null, errorMessage = null) }
    }

    fun onAdminPasswordChanged(value: String) {
        _state.update { it.copy(adminPassword = value, adminPasswordError = null, errorMessage = null) }
    }

    fun onSubmit() {
        val snapshot = _state.value
        val churchNameError = snapshot.churchName.takeIf { it.isBlank() }?.let { "Nama gereja wajib diisi" }
        val adminFullNameError = snapshot.adminFullName.takeIf { it.isBlank() }?.let { "Nama admin wajib diisi" }
        val adminEmailError = when {
            snapshot.adminEmail.isBlank() -> "Email admin wajib diisi"
            !snapshot.adminEmail.contains("@") -> "Email tidak valid"
            else -> null
        }
        val adminPasswordError = snapshot.adminPassword
            .takeIf { it.length < 8 }
            ?.let { "Password minimal 8 karakter" }

        if (listOfNotNull(churchNameError, adminFullNameError, adminEmailError, adminPasswordError).isNotEmpty()) {
            _state.update {
                it.copy(
                    churchNameError = churchNameError,
                    adminFullNameError = adminFullNameError,
                    adminEmailError = adminEmailError,
                    adminPasswordError = adminPasswordError
                )
            }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.registerNewTenant(
                churchName = snapshot.churchName.trim(),
                denomination = snapshot.denomination.trim(),
                adminFullName = snapshot.adminFullName.trim(),
                adminEmail = snapshot.adminEmail.trim(),
                adminPassword = snapshot.adminPassword,
                terminologyPreset = snapshot.terminologyPreset.trim()
            )
            when (result) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            registeredRole = result.data.role
                        )
                    }
                }
                is AppResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun consumeNavigation() {
        _state.update { it.copy(registeredRole = null) }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
