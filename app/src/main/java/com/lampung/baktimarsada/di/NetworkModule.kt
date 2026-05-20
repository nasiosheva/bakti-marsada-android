package com.lampung.baktimarsada.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.lampung.baktimarsada.core.constants.AppBuildConfig
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.network.api.BaktiApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.lampung.baktimarsada.security.SecureStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptorQualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TenantInterceptorQualifier

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }


    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    @AuthInterceptorQualifier
    fun provideAuthInterceptor(secureStorage: SecureStorage): Interceptor {
        return Interceptor { chain ->
            val builder = chain.request().newBuilder()
            val token = secureStorage.getString(AppConstants.KEY_AUTH_TOKEN).orEmpty()
            if (token.isNotBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
            chain.proceed(builder.build())
        }
    }

    @Provides
    @Singleton
    @TenantInterceptorQualifier
    fun provideTenantInterceptor(): Interceptor {
        return Interceptor { chain ->
            val builder = chain.request().newBuilder()
            val tenantId = runCatching { TenantRuntime.current.tenantId }.getOrNull().orEmpty()
            if (tenantId.isNotBlank()) {
                builder.header(AppConstants.HEADER_TENANT_ID, tenantId)
            }
            chain.proceed(builder.build())
        }
    }

    @Provides
    @Singleton
    fun provideChuckerInterceptor(
        @ApplicationContext context: Context
    ): ChuckerInterceptor {
        return ChuckerInterceptor.Builder(context)
            .maxContentLength(250_000L)
            .alwaysReadResponseBody(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        @AuthInterceptorQualifier authInterceptor: Interceptor,
        @TenantInterceptorQualifier tenantInterceptor: Interceptor,
        chuckerInterceptor: ChuckerInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(tenantInterceptor)
            .addInterceptor(chuckerInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named(AppConstants.QUALIFIER_CLOUDFLARE_API)
    fun provideCloudflareRetrofit(
        client: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppBuildConfig.cloudflareBaseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named(AppConstants.QUALIFIER_PYTHON_API)
    fun providePythonRetrofit(
        client: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppBuildConfig.pythonBaseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named(AppConstants.QUALIFIER_CLOUDFLARE_API)
    fun provideCloudflareApiService(
        @Named(AppConstants.QUALIFIER_CLOUDFLARE_API) retrofit: Retrofit
    ): BaktiApiService {
        return retrofit.create(BaktiApiService::class.java)
    }

    @Provides
    @Singleton
    @Named(AppConstants.QUALIFIER_PYTHON_API)
    fun providePythonApiService(
        @Named(AppConstants.QUALIFIER_PYTHON_API) retrofit: Retrofit
    ): BaktiApiService {
        return retrofit.create(BaktiApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAppRemoteDataSource(
        selector: AppRemoteDataSourceSelector
    ): AppRemoteDataSource {
        return selector.select(
            simulationEnabled = AppBuildConfig.simulationEnabled,
            provider = AppBuildConfig.dataSourceProvider
        )
    }
}
