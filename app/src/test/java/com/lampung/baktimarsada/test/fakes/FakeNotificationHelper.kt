package com.lampung.baktimarsada.test.fakes

import com.google.firebase.messaging.RemoteMessage
import com.lampung.baktimarsada.firebase.notification.NotificationHelper

class FakeNotificationHelper : NotificationHelper {
    var lastAccountLabel: String? = null
    var shownNotificationCount: Int = 0

    override fun showNotification(message: RemoteMessage) {
        shownNotificationCount += 1
    }

    override fun showGoogleWelcomeNotification(accountLabel: String) {
        lastAccountLabel = accountLabel
    }
}
