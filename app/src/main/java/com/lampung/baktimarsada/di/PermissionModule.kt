package com.lampung.baktimarsada.di

import com.lampung.baktimarsada.permission.AndroidPermissionManager
import com.lampung.baktimarsada.permission.PermissionManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PermissionModule {

    @Binds
    @Singleton
    abstract fun bindPermissionManager(impl: AndroidPermissionManager): PermissionManager
}

// created by Mories Deo Hutapea, S.E.,S.Kom
