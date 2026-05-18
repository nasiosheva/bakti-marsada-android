package com.lampung.baktimarsada.test.fakes

import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.network.dto.CreateUserAccountRequestDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountResponseDto
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.GoogleLoginRequestDto
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
import com.lampung.baktimarsada.network.dto.WorshipTemplateDto

open class UnsupportedAppRemoteDataSource : AppRemoteDataSource {
    override suspend fun login(request: LoginRequestDto): SessionResponseDto = unsupported()
    override suspend fun loginWithGoogle(request: GoogleLoginRequestDto): SessionResponseDto = unsupported()
    override suspend fun logout(token: String): Unit = Unit
    override suspend fun syncFcmToken(token: String): Unit = Unit
    override suspend fun fetchEvents(sectorId: String): List<EventDto> = unsupported()
    override suspend fun saveEvent(event: EventDto): EventDto = unsupported()
    override suspend fun deleteEvent(eventId: String): Unit = unsupported()
    override suspend fun fetchMembers(sectorId: String): List<MemberDto> = unsupported()
    override suspend fun saveMember(member: MemberDto): MemberDto = unsupported()
    override suspend fun deleteMember(memberId: String): Unit = unsupported()
    override suspend fun fetchFinanceReports(sectorId: String): List<FinanceReportDto> = unsupported()
    override suspend fun saveFinanceReport(report: FinanceReportDto): FinanceReportDto = unsupported()
    override suspend fun deleteFinanceReport(reportId: String): Unit = unsupported()
    override suspend fun fetchPaymentObligations(sectorId: String): List<PaymentObligationDto> = unsupported()
    override suspend fun savePaymentObligation(obligation: PaymentObligationDto): PaymentObligationDto = unsupported()
    override suspend fun deletePaymentObligation(obligationId: String): Unit = unsupported()
    override suspend fun fetchWorshipTemplates(sectorId: String): List<WorshipTemplateDto> = unsupported()
    override suspend fun saveWorshipTemplate(template: WorshipTemplateDto): WorshipTemplateDto = unsupported()
    override suspend fun deleteWorshipTemplate(templateId: String): Unit = unsupported()
    override suspend fun createUserAccount(request: CreateUserAccountRequestDto): CreateUserAccountResponseDto = unsupported()
    override suspend fun fetchUsersByRole(role: String): List<CreateUserAccountResponseDto> = unsupported()
    override suspend fun resetSimulationData(): Unit = Unit

    protected fun unsupported(): Nothing = error("unsupported in this test")
}
