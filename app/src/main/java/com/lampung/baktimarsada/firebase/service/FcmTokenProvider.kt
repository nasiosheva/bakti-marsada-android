package com.lampung.baktimarsada.firebase.service

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface FcmTokenProvider {
    suspend fun getToken(): String
}

@Singleton
class FirebaseFcmTokenProvider @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging
) : FcmTokenProvider {
    override suspend fun getToken(): String = firebaseMessaging.token.await()
}

// created by Mories Deo Hutapea, S.E.,S.Kom
