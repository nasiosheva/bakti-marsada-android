package com.lampung.baktimarsada.security

import com.lampung.baktimarsada.core.constants.AppConstants
import net.sqlcipher.database.SQLiteDatabase
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface DatabaseEncryptionProvider {
    fun getOrCreatePassphrase(): ByteArray
}

@Singleton
class DatabaseEncryptionProviderImpl @Inject constructor(
    private val secureStorage: SecureStorage
) : DatabaseEncryptionProvider {

    override fun getOrCreatePassphrase(): ByteArray {
        val storedPassphrase = secureStorage.getString(AppConstants.KEY_DB_PASSPHRASE)
            ?: generatePassphrase().also { generated ->
                secureStorage.putString(AppConstants.KEY_DB_PASSPHRASE, generated)
            }
        return SQLiteDatabase.getBytes(storedPassphrase.toCharArray())
    }

    private fun generatePassphrase(): String {
        return UUID.randomUUID().toString() + System.currentTimeMillis()
    }
}
