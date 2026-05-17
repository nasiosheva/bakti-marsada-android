package com.lampung.baktimarsada.domain.model

data class BaktiFormData(
    val fullName: String,
    val selectedGender: String,
    val selectedRole: String,
    val selectedInterests: List<String>,
    val acceptedTerms: Boolean
)

// created by Mories Deo Hutapea, S.E.,S.Kom
