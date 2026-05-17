package com.lampung.baktimarsada.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.lampung.baktimarsada.firebase.service.FirebaseTelemetryService
import com.lampung.baktimarsada.firebase.service.FirebaseTelemetryServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseTelemetryService(
        firestore: FirebaseFirestore
    ): FirebaseTelemetryService = FirebaseTelemetryServiceImpl(firestore)
}

// created by Mories Deo Hutapea, S.E.,S.Kom
