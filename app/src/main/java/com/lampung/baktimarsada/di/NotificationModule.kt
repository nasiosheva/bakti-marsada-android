package com.lampung.baktimarsada.di

import com.lampung.baktimarsada.firebase.notification.FcmNotificationHelper
import com.lampung.baktimarsada.firebase.notification.NotificationHelper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindNotificationHelper(
        impl: FcmNotificationHelper
    ): NotificationHelper
}
