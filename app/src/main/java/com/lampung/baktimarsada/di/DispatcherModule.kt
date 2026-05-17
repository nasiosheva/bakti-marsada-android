package com.lampung.baktimarsada.di

import com.lampung.baktimarsada.core.dispatchers.DefaultDispatcherProvider
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}

// created by Mories Deo Hutapea, S.E.,S.Kom
