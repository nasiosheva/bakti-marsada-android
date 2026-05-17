package com.lampung.baktimarsada.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.lampung.baktimarsada.core.constants.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface SecureStorage {
    fun putString(key: String, value: String)
    fun getString(key: String): String?
    fun remove(key: String)
}

@Singleton
class EncryptedSecureStorage @Inject constructor(
    @ApplicationContext context: Context
) : SecureStorage {

    private val preferences = createEncryptedPrefs(context)

    override fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    @Suppress("DEPRECATION")
    private fun createEncryptedPrefs(context: Context) = EncryptedSharedPreferences.create(
        AppConstants.ENCRYPTED_PREFS_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

// created by Mories Deo Hutapea, S.E.,S.Kom
