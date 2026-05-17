package com.lampung.baktimarsada.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.data.remote.BackendAppRemoteDataSource
import com.lampung.baktimarsada.data.remote.SimulateAppRemoteDataSource
import com.lampung.baktimarsada.network.api.BaktiApiService
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
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
        authInterceptor: Interceptor,
        chuckerInterceptor: ChuckerInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(chuckerInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConstants.NETWORK_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideBaktiApiService(retrofit: Retrofit): BaktiApiService {
        return retrofit.create(BaktiApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAppRemoteDataSource(
        simulateDataSource: SimulateAppRemoteDataSource,
        backendDataSource: BackendAppRemoteDataSource
    ): AppRemoteDataSource {
        return if (AppConstants.SIMULATION_ENABLED) {
            simulateDataSource
        } else {
            backendDataSource
        }
    }
}
