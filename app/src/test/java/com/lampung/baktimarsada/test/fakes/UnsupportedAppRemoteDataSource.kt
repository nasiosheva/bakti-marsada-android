package com.lampung.baktimarsada.test.fakes

import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.network.dto.CreateUserAccountRequestDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountResponseDto
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.GoogleLoginRequestDto
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.network.dto.ArisanParticipantDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.RegisterTenantRequestDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
import com.lampung.baktimarsada.network.dto.TenantProfileDto
import com.lampung.baktimarsada.network.dto.WorshipTemplateDto

open class UnsupportedAppRemoteDataSource : AppRemoteDataSource {
    override suspend fun login(request: LoginRequestDto): SessionResponseDto = unsupported()
    override suspend fun loginWithGoogle(request: GoogleLoginRequestDto): SessionResponseDto = unsupported()
    override suspend fun registerNewTenant(request: RegisterTenantRequestDto): SessionResponseDto = unsupported()
    override suspend fun logout(token: String): Unit = Unit
    override suspend fun syncFcmToken(token: String): Unit = Unit
    override suspend fun fetchEvents(tenantId: String, sectorId: String): List<EventDto> = unsupported()
    override suspend fun saveEvent(tenantId: String, event: EventDto): EventDto = unsupported()
    override suspend fun deleteEvent(tenantId: String, eventId: String): Unit = unsupported()
    override suspend fun fetchMembers(tenantId: String, sectorId: String): List<MemberDto> = unsupported()
    override suspend fun saveMember(tenantId: String, member: MemberDto): MemberDto = unsupported()
    override suspend fun deleteMember(tenantId: String, memberId: String): Unit = unsupported()
    override suspend fun fetchFinanceReports(tenantId: String, sectorId: String): List<FinanceReportDto> = unsupported()
    override suspend fun saveFinanceReport(tenantId: String, report: FinanceReportDto): FinanceReportDto = unsupported()
    override suspend fun deleteFinanceReport(tenantId: String, reportId: String): Unit = unsupported()
    override suspend fun fetchPaymentObligations(tenantId: String, sectorId: String): List<PaymentObligationDto> = unsupported()
    override suspend fun savePaymentObligation(tenantId: String, obligation: PaymentObligationDto): PaymentObligationDto = unsupported()
    override suspend fun deletePaymentObligation(tenantId: String, obligationId: String): Unit = unsupported()
    override suspend fun fetchArisanParticipants(tenantId: String, sectorId: String): List<ArisanParticipantDto> = unsupported()
    override suspend fun replaceArisanParticipants(tenantId: String, sectorId: String, memberIds: List<String>): List<ArisanParticipantDto> = unsupported()
    override suspend fun fillArisanParticipantsFromMembers(tenantId: String, sectorId: String): List<ArisanParticipantDto> = unsupported()
    override suspend fun fetchWorshipTemplates(tenantId: String, sectorId: String): List<WorshipTemplateDto> = unsupported()
    override suspend fun saveWorshipTemplate(tenantId: String, template: WorshipTemplateDto): WorshipTemplateDto = unsupported()
    override suspend fun deleteWorshipTemplate(tenantId: String, templateId: String): Unit = unsupported()
    override suspend fun createUserAccount(request: CreateUserAccountRequestDto): CreateUserAccountResponseDto = unsupported()
    override suspend fun fetchUsersByRole(role: String): List<CreateUserAccountResponseDto> = unsupported()
    override suspend fun fetchTenantProfile(tenantId: String): TenantProfileDto = unsupported()
    override suspend fun resetSimulationData(): Unit = Unit

    protected fun unsupported(): Nothing = error("unsupported in this test")
}
