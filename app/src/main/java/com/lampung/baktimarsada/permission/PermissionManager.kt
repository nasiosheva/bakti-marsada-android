package com.lampung.baktimarsada.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface PermissionManager {
    val notificationPermission: String
    fun isNotificationGranted(): Boolean
    fun shouldRequestNotificationPermission(): Boolean
}

@Singleton
class AndroidPermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PermissionManager {

    override val notificationPermission: String = Manifest.permission.POST_NOTIFICATIONS

    override fun isNotificationGranted(): Boolean {
        if (!shouldRequestNotificationPermission()) {
            return true
        }
        return ContextCompat.checkSelfPermission(
            context,
            notificationPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun shouldRequestNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }
}
