package com.lampung.baktimarsada.di

import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.core.constants.DataSourceProvider
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AppRemoteDataSourceSelector @Inject constructor(
    @Named(AppConstants.QUALIFIER_SIMULATE_SOURCE)
    private val simulateDataSource: AppRemoteDataSource,
    private val realDataSources: Map<DataSourceProvider, @JvmSuppressWildcards Provider<AppRemoteDataSource>>
) {
    fun select(
        simulationEnabled: Boolean,
        provider: DataSourceProvider
    ): AppRemoteDataSource {
        if (simulationEnabled) return simulateDataSource
        return realDataSources[provider]?.get()
            ?: error("No AppRemoteDataSource binding found for provider=${provider.name}")
    }
}
