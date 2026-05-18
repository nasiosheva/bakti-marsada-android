package com.lampung.baktimarsada.firebase.core.di

import android.content.Context
import android.content.pm.ApplicationInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.lampung.baktimarsada.firebase.core.DefaultFirebaseRuntimeAvailability
import com.lampung.baktimarsada.firebase.core.FirebaseRuntimeAvailability
import com.lampung.baktimarsada.firebase.core.remoteconfig.DefaultTenantAwareRemoteConfigResolver
import com.lampung.baktimarsada.firebase.core.remoteconfig.FirebaseRemoteConfigBootstrap
import com.lampung.baktimarsada.firebase.core.remoteconfig.FirebaseRemoteConfigClient
import com.lampung.baktimarsada.firebase.core.remoteconfig.FirebaseRemoteConfigService
import com.lampung.baktimarsada.firebase.core.remoteconfig.RemoteConfigBootstrap
import com.lampung.baktimarsada.firebase.core.remoteconfig.RemoteConfigClient
import com.lampung.baktimarsada.firebase.core.remoteconfig.RemoteConfigDefaultsProvider
import com.lampung.baktimarsada.firebase.core.remoteconfig.RemoteConfigFetchPolicy
import com.lampung.baktimarsada.firebase.core.remoteconfig.RemoteConfigService
import com.lampung.baktimarsada.firebase.core.remoteconfig.TenantAwareRemoteConfigResolver
import com.lampung.baktimarsada.firebase.core.remoteconfig.XmlRemoteConfigDefaultsProvider
import com.lampung.baktimarsada.firebase.core.service.FcmTokenProvider
import com.lampung.baktimarsada.firebase.core.service.FirebaseFcmTokenProvider
import com.lampung.baktimarsada.firebase.core.service.FirebaseTelemetryService
import com.lampung.baktimarsada.firebase.core.service.FirebaseTelemetryServiceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseCoreBindingModule {

    @Binds
    @Singleton
    abstract fun bindFirebaseRuntimeAvailability(
        impl: DefaultFirebaseRuntimeAvailability
    ): FirebaseRuntimeAvailability

    @Binds
    @Singleton
    abstract fun bindRemoteConfigDefaultsProvider(
        impl: XmlRemoteConfigDefaultsProvider
    ): RemoteConfigDefaultsProvider

    @Binds
    @Singleton
    abstract fun bindTenantAwareRemoteConfigResolver(
        impl: DefaultTenantAwareRemoteConfigResolver
    ): TenantAwareRemoteConfigResolver

    @Binds
    @Singleton
    abstract fun bindRemoteConfigClient(
        impl: FirebaseRemoteConfigClient
    ): RemoteConfigClient

    @Binds
    @Singleton
    abstract fun bindRemoteConfigService(
        impl: FirebaseRemoteConfigService
    ): RemoteConfigService

    @Binds
    @Singleton
    abstract fun bindRemoteConfigBootstrap(
        impl: FirebaseRemoteConfigBootstrap
    ): RemoteConfigBootstrap

    @Binds
    @Singleton
    abstract fun bindFcmTokenProvider(
        impl: FirebaseFcmTokenProvider
    ): FcmTokenProvider

    @Binds
    @Singleton
    abstract fun bindFirebaseTelemetryService(
        impl: FirebaseTelemetryServiceImpl
    ): FirebaseTelemetryService
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseCoreModule {

    @Provides
    @Singleton
    fun provideRemoteConfigFetchPolicy(
        @ApplicationContext context: Context
    ): RemoteConfigFetchPolicy {
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return RemoteConfigFetchPolicy(
            minimumFetchIntervalSeconds = if (isDebuggable) 60L else 3600L
        )
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(
        runtimeAvailability: FirebaseRuntimeAvailability
    ): FirebaseFirestore? {
        return runtimeAvailability.firebaseApp()?.let { FirebaseFirestore.getInstance() }
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(
        runtimeAvailability: FirebaseRuntimeAvailability
    ): FirebaseMessaging? {
        return runtimeAvailability.firebaseApp()?.let { FirebaseMessaging.getInstance() }
    }

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(
        runtimeAvailability: FirebaseRuntimeAvailability
    ): FirebaseRemoteConfig? {
        return runtimeAvailability.firebaseApp()?.let { FirebaseRemoteConfig.getInstance() }
    }
}
