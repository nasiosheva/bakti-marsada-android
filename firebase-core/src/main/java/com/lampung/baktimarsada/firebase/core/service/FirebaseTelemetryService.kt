package com.lampung.baktimarsada.firebase.core.service

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.lampung.baktimarsada.core.constants.AppConstants
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface FirebaseTelemetryService {
    suspend fun logEvent(type: String, payload: Map<String, Any?> = emptyMap())
}

@Singleton
class FirebaseTelemetryServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore?
) : FirebaseTelemetryService {
    override suspend fun logEvent(type: String, payload: Map<String, Any?>) {
        val firestoreInstance = firestore ?: return
        val eventPayload = buildMap<String, Any?> {
            put(AppConstants.FIRESTORE_FIELD_EVENT_TYPE, type)
            put(AppConstants.FIRESTORE_FIELD_TIMESTAMP, FieldValue.serverTimestamp())
            putAll(payload)
        }
        firestoreInstance
            .collection(AppConstants.FIRESTORE_COLLECTION_TRACKING)
            .add(eventPayload)
            .await()
    }
}
