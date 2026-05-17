package com.lampung.baktimarsada.datasource.cloudflare

import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.core.constants.DataSourceProvider
import com.lampung.baktimarsada.core.di.DataSourceBindingKey
import com.lampung.baktimarsada.data.remote.RestAppRemoteDataSource
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.network.api.BaktiApiService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CloudflareAppRemoteDataSource @Inject constructor(
    @Named(AppConstants.QUALIFIER_CLOUDFLARE_API)
    apiService: BaktiApiService
) : RestAppRemoteDataSource(apiService)

@Module
@InstallIn(SingletonComponent::class)
abstract class CloudflareDataSourceModule {

    @Binds
    @IntoMap
    @DataSourceBindingKey(DataSourceProvider.CLOUDFLARE)
    abstract fun bindCloudflareDataSource(impl: CloudflareAppRemoteDataSource): AppRemoteDataSource
}
