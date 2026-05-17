package com.lampung.baktimarsada.di

import com.lampung.baktimarsada.data.repository.AuthRepositoryImpl
import com.lampung.baktimarsada.data.repository.EventRepositoryImpl
import com.lampung.baktimarsada.data.repository.FinanceReportRepositoryImpl
import com.lampung.baktimarsada.data.repository.MemberRepositoryImpl
import com.lampung.baktimarsada.data.repository.PaymentObligationRepositoryImpl
import com.lampung.baktimarsada.domain.repository.AuthRepository
import com.lampung.baktimarsada.domain.repository.EventRepository
import com.lampung.baktimarsada.domain.repository.FinanceReportRepository
import com.lampung.baktimarsada.domain.repository.MemberRepository
import com.lampung.baktimarsada.domain.repository.PaymentObligationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        impl: EventRepositoryImpl
    ): EventRepository

    @Binds
    @Singleton
    abstract fun bindMemberRepository(
        impl: MemberRepositoryImpl
    ): MemberRepository

    @Binds
    @Singleton
    abstract fun bindFinanceReportRepository(
        impl: FinanceReportRepositoryImpl
    ): FinanceReportRepository

    @Binds
    @Singleton
    abstract fun bindPaymentObligationRepository(
        impl: PaymentObligationRepositoryImpl
    ): PaymentObligationRepository

}
