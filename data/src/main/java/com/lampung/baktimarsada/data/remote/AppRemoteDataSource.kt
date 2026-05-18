package com.lampung.baktimarsada.data.remote

import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountRequestDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountResponseDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
import com.lampung.baktimarsada.network.dto.WorshipTemplateDto

interface AppRemoteDataSource {
    suspend fun login(request: LoginRequestDto): SessionResponseDto
    suspend fun logout(token: String)
    suspend fun syncFcmToken(token: String)
    suspend fun fetchEvents(sectorId: String): List<EventDto>
    suspend fun saveEvent(event: EventDto): EventDto
    suspend fun deleteEvent(eventId: String)
    suspend fun fetchMembers(sectorId: String): List<MemberDto>
    suspend fun saveMember(member: MemberDto): MemberDto
    suspend fun deleteMember(memberId: String)
    suspend fun fetchFinanceReports(sectorId: String): List<FinanceReportDto>
    suspend fun saveFinanceReport(report: FinanceReportDto): FinanceReportDto
    suspend fun deleteFinanceReport(reportId: String)
    suspend fun fetchPaymentObligations(sectorId: String): List<PaymentObligationDto>
    suspend fun savePaymentObligation(obligation: PaymentObligationDto): PaymentObligationDto
    suspend fun deletePaymentObligation(obligationId: String)
    suspend fun fetchWorshipTemplates(sectorId: String): List<WorshipTemplateDto>
    suspend fun saveWorshipTemplate(template: WorshipTemplateDto): WorshipTemplateDto
    suspend fun deleteWorshipTemplate(templateId: String)
    suspend fun createUserAccount(request: CreateUserAccountRequestDto): CreateUserAccountResponseDto
    suspend fun fetchUsersByRole(role: String): List<CreateUserAccountResponseDto>
    suspend fun resetSimulationData()
}

// created by Mories Deo Hutapea, S.E.,S.Kom
