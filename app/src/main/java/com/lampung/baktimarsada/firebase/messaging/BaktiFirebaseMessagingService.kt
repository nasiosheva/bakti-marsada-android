package com.lampung.baktimarsada.firebase.messaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.firebase.notification.FcmNotificationHelper
import com.lampung.baktimarsada.domain.usecase.SyncFcmTokenUseCase
import com.lampung.baktimarsada.firebase.service.FirebaseTelemetryService
import com.lampung.baktimarsada.security.SecureStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BaktiFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var syncFcmTokenUseCase: SyncFcmTokenUseCase

    @Inject
    lateinit var secureStorage: SecureStorage

    @Inject
    lateinit var telemetryService: FirebaseTelemetryService
    @Inject
    lateinit var notificationHelper: FcmNotificationHelper

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        secureStorage.putString(AppConstants.KEY_FCM_TOKEN, token)
        serviceScope.launch {
            syncFcmTokenUseCase(token)
            telemetryService.logEvent(
                type = AppConstants.TRACKING_EVENT_TOKEN_REFRESHED,
                payload = mapOf(AppConstants.FIRESTORE_FIELD_TOKEN_LENGTH to token.length)
            )
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        serviceScope.launch {
            notificationHelper.showNotification(message)
            telemetryService.logEvent(
                type = AppConstants.TRACKING_EVENT_MESSAGE_RECEIVED,
                payload = mapOf(
                    AppConstants.FIRESTORE_FIELD_SOURCE to (message.from ?: AppConstants.VALUE_UNKNOWN),
                    AppConstants.FIRESTORE_FIELD_HAS_DATA to message.data.isNotEmpty(),
                    AppConstants.FIRESTORE_FIELD_NOTIFICATION_TITLE to (message.notification?.title ?: AppConstants.VALUE_EMPTY)
                )
            )
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
