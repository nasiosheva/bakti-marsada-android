package com.lampung.baktimarsada.feature.roulette.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.lampung.baktimarsada.feature.roulette.data.AppRouletteLocalStore
import com.lampung.baktimarsada.feature.roulette.data.RouletteLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
abstract class RouletteStorageModule {

    @Binds
    @Singleton
    abstract fun bindRouletteLocalDataSource(
        impl: AppRouletteLocalStore
    ): RouletteLocalDataSource

    @Module
    @InstallIn(SingletonComponent::class)
    object Provider {

        @Provides
        @Singleton
        fun provideRouletteDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create(
                scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
                produceFile = { context.preferencesDataStoreFile("roulette.preferences_pb") }
            )
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
