package com.lampung.baktimarsada.data.remote

import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.GoogleLoginRequestDto
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.network.dto.ArisanParticipantDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountRequestDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountResponseDto
import com.lampung.baktimarsada.network.dto.RegisterTenantRequestDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
import com.lampung.baktimarsada.network.dto.TenantProfileDto
import com.lampung.baktimarsada.network.dto.WorshipTemplateDto

interface AppRemoteDataSource {
    suspend fun login(request: LoginRequestDto): SessionResponseDto
    suspend fun loginWithGoogle(request: GoogleLoginRequestDto): SessionResponseDto
    suspend fun registerNewTenant(request: RegisterTenantRequestDto): SessionResponseDto
    suspend fun logout(token: String)
    suspend fun syncFcmToken(token: String)
    suspend fun fetchEvents(tenantId: String, sectorId: String): List<EventDto>
    suspend fun saveEvent(tenantId: String, event: EventDto): EventDto
    suspend fun deleteEvent(tenantId: String, eventId: String)
    suspend fun fetchMembers(tenantId: String, sectorId: String): List<MemberDto>
    suspend fun saveMember(tenantId: String, member: MemberDto): MemberDto
    suspend fun deleteMember(tenantId: String, memberId: String)
    suspend fun fetchFinanceReports(tenantId: String, sectorId: String): List<FinanceReportDto>
    suspend fun saveFinanceReport(tenantId: String, report: FinanceReportDto): FinanceReportDto
    suspend fun deleteFinanceReport(tenantId: String, reportId: String)
    suspend fun fetchPaymentObligations(tenantId: String, sectorId: String): List<PaymentObligationDto>
    suspend fun savePaymentObligation(tenantId: String, obligation: PaymentObligationDto): PaymentObligationDto
    suspend fun deletePaymentObligation(tenantId: String, obligationId: String)
    suspend fun fetchArisanParticipants(tenantId: String, sectorId: String): List<ArisanParticipantDto>
    suspend fun replaceArisanParticipants(tenantId: String, sectorId: String, memberIds: List<String>): List<ArisanParticipantDto>
    suspend fun fillArisanParticipantsFromMembers(tenantId: String, sectorId: String): List<ArisanParticipantDto>
    suspend fun fetchWorshipTemplates(tenantId: String, sectorId: String): List<WorshipTemplateDto>
    suspend fun saveWorshipTemplate(tenantId: String, template: WorshipTemplateDto): WorshipTemplateDto
    suspend fun deleteWorshipTemplate(tenantId: String, templateId: String)
    suspend fun createUserAccount(request: CreateUserAccountRequestDto): CreateUserAccountResponseDto
    suspend fun fetchUsersByRole(role: String): List<CreateUserAccountResponseDto>
    suspend fun fetchTenantProfile(tenantId: String): TenantProfileDto
    suspend fun resetSimulationData()
}

// created by Mories Deo Hutapea, S.E.,S.Kom
