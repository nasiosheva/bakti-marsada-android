package com.lampung.baktimarsada.feature.onboarding.presentation

import com.lampung.baktimarsada.domain.model.UserRole

data class RegisterChurchUiState(
    val churchName: String = "",
    val denomination: String = DEFAULT_DENOMINATION,
    val terminologyPreset: String = DEFAULT_TERMINOLOGY,
    val adminFullName: String = "",
    val adminEmail: String = "",
    val adminPassword: String = "",

    val churchNameError: String? = null,
    val adminFullNameError: String? = null,
    val adminEmailError: String? = null,
    val adminPasswordError: String? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registeredRole: UserRole? = null
) {
    val canSubmit: Boolean
        get() = !isLoading &&
            churchName.isNotBlank() &&
            adminFullName.isNotBlank() &&
            adminEmail.isNotBlank() &&
            adminPassword.length >= 8

    companion object {
        const val DEFAULT_DENOMINATION = "HKBP"
        const val DEFAULT_TERMINOLOGY = "HKBP"

        val DENOMINATION_OPTIONS = listOf("HKBP", "Katolik", "GBI", "GKI", "Lainnya")
        val TERMINOLOGY_OPTIONS = listOf("HKBP", "Katolik", "GBI", "Generik")
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
