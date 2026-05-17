package com.lampung.baktimarsada.di

import android.content.Context
import androidx.room.Room
import com.lampung.baktimarsada.core.constants.AppBuildConfig
import com.lampung.baktimarsada.db.AppDatabase
import com.lampung.baktimarsada.db.dao.EventDao
import com.lampung.baktimarsada.db.dao.FinanceReportDao
import com.lampung.baktimarsada.db.dao.MemberDao
import com.lampung.baktimarsada.db.dao.PaymentObligationDao
import com.lampung.baktimarsada.security.DatabaseEncryptionProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        encryptionProvider: DatabaseEncryptionProvider
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppBuildConfig.databaseName
        )
            .openHelperFactory(net.sqlcipher.database.SupportFactory(encryptionProvider.getOrCreatePassphrase()))
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideEventDao(database: AppDatabase): EventDao = database.eventDao()

    @Provides
    fun provideMemberDao(database: AppDatabase): MemberDao = database.memberDao()

    @Provides
    fun provideFinanceReportDao(database: AppDatabase): FinanceReportDao = database.financeReportDao()

    @Provides
    fun providePaymentObligationDao(database: AppDatabase): PaymentObligationDao =
        database.paymentObligationDao()

}

// created by Mories Deo Hutapea, S.E.,S.Kom
