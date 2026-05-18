package com.lampung.baktimarsada.firebase.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.MainActivity
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.feature.app.navigation.AppRoutes
import com.lampung.baktimarsada.permission.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface NotificationHelper {
    fun showNotification(message: RemoteMessage)
    fun showGoogleWelcomeNotification(accountLabel: String)
}

class FcmNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) : NotificationHelper {

    override fun showNotification(message: RemoteMessage) {
        if (!permissionManager.isNotificationGranted()) return
        val title = resolveTitle(message)
        val contentText = resolveContent(message)
        if (title.isBlank() && contentText.isBlank()) return

        ensureNotificationChannel()

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val deepLink = message.data[AppConstants.FCM_NOTIFICATION_DEEP_LINK]
            val route = message.data[AppConstants.FCM_NOTIFICATION_ROUTE]
            if (!deepLink.isNullOrBlank()) {
                data = Uri.parse(deepLink)
            }
            if (!route.isNullOrBlank()) {
                putExtra(AppConstants.FCM_NOTIFICATION_ROUTE_EXTRA, route)
            }
            val eventId = message.data[AppConstants.FCM_NOTIFICATION_EVENT_ID]
            if (!eventId.isNullOrBlank()) {
                putExtra(AppConstants.FCM_NOTIFICATION_EVENT_ID_EXTRA, eventId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            AppConstants.FCM_NOTIFICATION_ID_BASE.hashCode(),
            notificationIntent,
            createPendingIntentFlags()
        )

        val notification = NotificationCompat.Builder(context, AppConstants.FCM_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title.ifBlank { context.getString(R.string.app_name) })
            .setContentText(contentText)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        )
        notificationManager?.notify(generateNotificationId(message), notification)
    }

    override fun showGoogleWelcomeNotification(accountLabel: String) {
        if (!permissionManager.isNotificationGranted()) return
        val resolvedAccountLabel = accountLabel.trim().ifBlank {
            context.getString(R.string.login_google_fallback_account_label)
        }
        ensureNotificationChannel()

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(AppConstants.FCM_NOTIFICATION_ROUTE_EXTRA, AppRoutes.JEMAAT_HOME)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            AppConstants.FCM_NOTIFICATION_ID_BASE.hashCode() + 1,
            notificationIntent,
            createPendingIntentFlags()
        )

        val notification = NotificationCompat.Builder(context, AppConstants.FCM_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.login_google_welcome_title))
            .setContentText(
                context.getString(
                    R.string.login_google_welcome_message,
                    resolvedAccountLabel
                )
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        )
        notificationManager?.notify(
            AppConstants.NOTIFICATION_ID_GOOGLE_WELCOME,
            notification
        )
    }

    private fun generateNotificationId(message: RemoteMessage): Int {
        val idFromPayload = message.data[AppConstants.FCM_NOTIFICATION_ID_BASE]
        val parsedId = idFromPayload?.toIntOrNull()
        return parsedId ?: (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }

    private fun resolveTitle(message: RemoteMessage): String {
        return message.notification?.title
            ?: message.data[AppConstants.FCM_NOTIFICATION_TITLE]
            ?: message.data[AppConstants.FCM_NOTIFICATION_MESSAGE]
            ?: message.data[AppConstants.FCM_NOTIFICATION_BODY]
            ?: ""
    }

    private fun resolveContent(message: RemoteMessage): String {
        return message.notification?.body
            ?: message.data[AppConstants.FCM_NOTIFICATION_MESSAGE]
            ?: message.data[AppConstants.FCM_NOTIFICATION_BODY]
            ?: ""
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) ?: return
        if (notificationManager.getNotificationChannel(AppConstants.FCM_NOTIFICATION_CHANNEL_ID) != null) {
            return
        }
        val channel = NotificationChannel(
            AppConstants.FCM_NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createPendingIntentFlags(): Int {
        val updateCurrentFlag = PendingIntent.FLAG_UPDATE_CURRENT
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            updateCurrentFlag or PendingIntent.FLAG_IMMUTABLE
        } else {
            updateCurrentFlag
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
