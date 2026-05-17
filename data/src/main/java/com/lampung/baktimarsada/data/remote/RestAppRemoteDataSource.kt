package com.lampung.baktimarsada.data.remote

import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.network.api.BaktiApiService
import com.lampung.baktimarsada.network.dto.BackendLoginRequestDto
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.FcmTokenRequestDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
import com.lampung.baktimarsada.network.dto.WorshipTemplateDto
import retrofit2.Response

abstract class RestAppRemoteDataSource(
    private val apiService: BaktiApiService
) : AppRemoteDataSource {

    override suspend fun login(request: LoginRequestDto): SessionResponseDto {
        val response = apiService.login(
            BackendLoginRequestDto(
                email = request.identifier.trim(),
                password = request.password
            )
        )
        val body = response.requireBody("Login failed")
        val token = body.session?.sessionToken ?: body.authToken
        val userId = body.user?.id ?: body.userId
        if (token.isNullOrBlank() || userId.isNullOrBlank()) {
            error("Login response is missing session token or user id")
        }

        val roleRaw = body.user?.role ?: body.role
        val role = when (roleRaw?.uppercase()) {
            "ADMIN" -> "ADMIN"
            "JEMAAT" -> "JEMAAT"
            else -> "JEMAAT"
        }

        return SessionResponseDto(
            authToken = token,
            userId = userId,
            displayName = body.user?.fullName ?: body.displayName ?: userId,
            role = role,
            tenantId = body.tenantId ?: TenantRuntime.current.tenantId,
            tenantName = body.tenantName ?: TenantRuntime.current.tenantName,
            subTenantId = body.subTenantId ?: TenantRuntime.current.subTenantId,
            subTenantName = body.subTenantName ?: TenantRuntime.current.subTenantName,
            sectorId = body.sectorId ?: TenantRuntime.current.defaultSectorId,
            sectorName = body.sectorName ?: TenantRuntime.current.defaultSectorName
        )
    }

    override suspend fun logout(token: String) {
        apiService.logout(authorization = "Bearer $token").requireSuccess("Logout failed")
    }

    override suspend fun syncFcmToken(token: String) {
        apiService.syncFcmToken(FcmTokenRequestDto(token = token)).requireSuccess("FCM token sync failed")
    }

    override suspend fun fetchEvents(sectorId: String): List<EventDto> {
        return apiService.fetchEvents(sectorId).requireBody("Failed to load events")
    }

    override suspend fun saveEvent(event: EventDto): EventDto {
        return apiService.saveEvent(event).requireBody("Failed to save event")
    }

    override suspend fun deleteEvent(eventId: String) {
        apiService.deleteEvent(eventId).requireSuccess("Failed to delete event")
    }

    override suspend fun fetchWorshipTemplates(sectorId: String): List<WorshipTemplateDto> {
        return apiService.fetchWorshipTemplates(sectorId).requireBody("Failed to load worship templates")
    }

    override suspend fun saveWorshipTemplate(template: WorshipTemplateDto): WorshipTemplateDto {
        return apiService.saveWorshipTemplate(template).requireBody("Failed to save worship template")
    }

    override suspend fun deleteWorshipTemplate(templateId: String) {
        apiService.deleteWorshipTemplate(templateId).requireSuccess("Failed to delete worship template")
    }

    override suspend fun fetchMembers(sectorId: String): List<MemberDto> {
        return apiService.fetchMembers(sectorId).requireBody("Failed to load members")
    }

    override suspend fun saveMember(member: MemberDto): MemberDto {
        return apiService.saveMember(member).requireBody("Failed to save member")
    }

    override suspend fun deleteMember(memberId: String) {
        apiService.deleteMember(memberId).requireSuccess("Failed to delete member")
    }

    override suspend fun fetchFinanceReports(sectorId: String): List<FinanceReportDto> {
        return apiService.fetchFinanceReports(sectorId).requireBody("Failed to load finance reports")
    }

    override suspend fun saveFinanceReport(report: FinanceReportDto): FinanceReportDto {
        return apiService.saveFinanceReport(report).requireBody("Failed to save finance report")
    }

    override suspend fun deleteFinanceReport(reportId: String) {
        apiService.deleteFinanceReport(reportId).requireSuccess("Failed to delete finance report")
    }

    override suspend fun fetchPaymentObligations(sectorId: String): List<PaymentObligationDto> {
        return apiService.fetchPaymentObligations(sectorId).requireBody("Failed to load payment obligations")
    }

    override suspend fun savePaymentObligation(obligation: PaymentObligationDto): PaymentObligationDto {
        return apiService.savePaymentObligation(obligation).requireBody("Failed to save payment obligation")
    }

    override suspend fun deletePaymentObligation(obligationId: String) {
        apiService.deletePaymentObligation(obligationId).requireSuccess("Failed to delete payment obligation")
    }

    override suspend fun resetSimulationData(): Unit = Unit

    private fun Response<*>.requireSuccess(defaultMessage: String) {
        if (!isSuccessful) {
            error(errorBody()?.string()?.ifBlank { null } ?: defaultMessage)
        }
    }

    private fun <T> Response<T>.requireBody(defaultMessage: String): T {
        if (!isSuccessful) {
            error(errorBody()?.string()?.ifBlank { null } ?: defaultMessage)
        }
        return body() ?: error(defaultMessage)
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
