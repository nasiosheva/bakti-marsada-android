package com.lampung.baktimarsada.di

import com.lampung.baktimarsada.firebase.service.FcmTokenProvider
import com.lampung.baktimarsada.firebase.service.FirebaseFcmTokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseBindingModule {

    @Binds
    @Singleton
    abstract fun bindFcmTokenProvider(impl: FirebaseFcmTokenProvider): FcmTokenProvider
}

// created by Mories Deo Hutapea, S.E.,S.Kom
