package com.lampung.baktimarsada.feature.roulette.di

import com.lampung.baktimarsada.feature.roulette.data.DefaultRouletteImportDataSource
import com.lampung.baktimarsada.feature.roulette.data.RouletteImportDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RouletteDataModule {

    @Binds
    @Singleton
    abstract fun bindRouletteImportDataSource(
        impl: DefaultRouletteImportDataSource
    ): RouletteImportDataSource
}

// created by Mories Deo Hutapea, S.E.,S.Kom
