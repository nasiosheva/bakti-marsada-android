package com.lampung.baktimarsada.firebase.core

import android.content.Context
import com.google.firebase.FirebaseApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface FirebaseRuntimeAvailability {
    fun isAvailable(): Boolean
    fun firebaseApp(): FirebaseApp?
}

@Singleton
class DefaultFirebaseRuntimeAvailability @Inject constructor(
    @ApplicationContext private val context: Context
) : FirebaseRuntimeAvailability {

    @Volatile
    private var cachedFirebaseApp: FirebaseApp? = null

    override fun isAvailable(): Boolean = firebaseApp() != null

    override fun firebaseApp(): FirebaseApp? {
        cachedFirebaseApp?.let { return it }
        synchronized(this) {
            cachedFirebaseApp?.let { return it }
            cachedFirebaseApp = FirebaseApp.getApps(context).firstOrNull()
                ?: FirebaseApp.initializeApp(context)
            return cachedFirebaseApp
        }
    }
}
