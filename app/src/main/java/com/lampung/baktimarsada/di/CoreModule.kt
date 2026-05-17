package com.lampung.baktimarsada.di

import com.lampung.baktimarsada.core.resources.AndroidStringProvider
import com.lampung.baktimarsada.core.resources.StringProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {

    @Binds
    @Singleton
    abstract fun bindStringProvider(impl: AndroidStringProvider): StringProvider
}

// created by Mories Deo Hutapea, S.E.,S.Kom
