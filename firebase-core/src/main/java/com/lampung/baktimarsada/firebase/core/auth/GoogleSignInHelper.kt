package com.lampung.baktimarsada.firebase.core.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.lampung.baktimarsada.core.constants.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

data class GoogleSignInToken(
    val idToken: String,
    val email: String?,
    val displayName: String?
)

interface GoogleSignInHelper {
    fun isAvailable(): Boolean
    fun createSignInIntent(): Intent?
    suspend fun extractToken(data: Intent?): Result<GoogleSignInToken>
}

@Singleton
class DefaultGoogleSignInHelper @Inject constructor(
    @ApplicationContext private val context: Context
) : GoogleSignInHelper {

    private val webClientId: String by lazy(LazyThreadSafetyMode.NONE) {
        resolveStringResource(AppConstants.RESOURCE_DEFAULT_WEB_CLIENT_ID)
            .ifBlank { resolveStringResource(AppConstants.RESOURCE_BAKTI_GOOGLE_WEB_CLIENT_ID) }
    }

    override fun isAvailable(): Boolean = webClientId.isNotBlank()

    override fun createSignInIntent(): Intent? {
        if (!isAvailable()) return null
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(webClientId)
            .build()
        return GoogleSignIn.getClient(context, options).signInIntent
    }

    override suspend fun extractToken(data: Intent?): Result<GoogleSignInToken> {
        return runCatching {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).await()
            account.requireToken()
        }
    }

    private fun GoogleSignInAccount.requireToken(): GoogleSignInToken {
        val token = idToken?.takeIf { it.isNotBlank() }
            ?: error("Google Sign-In token is unavailable")
        return GoogleSignInToken(
            idToken = token,
            email = email,
            displayName = displayName
        )
    }

    private fun resolveStringResource(name: String): String {
        val resId = context.resources.getIdentifier(name, "string", context.packageName)
        if (resId == 0) return ""
        return context.getString(resId).trim()
    }
}
