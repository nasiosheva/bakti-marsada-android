package com.lampung.baktimarsada.di

import com.lampung.baktimarsada.security.EncryptedSecureStorage
import com.lampung.baktimarsada.security.DatabaseEncryptionProvider
import com.lampung.baktimarsada.security.DatabaseEncryptionProviderImpl
import com.lampung.baktimarsada.security.SecureStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindSecureStorage(impl: EncryptedSecureStorage): SecureStorage

    @Binds
    @Singleton
    abstract fun bindDatabaseEncryptionProvider(
        impl: DatabaseEncryptionProviderImpl
    ): DatabaseEncryptionProvider
}
