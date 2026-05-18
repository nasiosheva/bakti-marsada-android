package com.lampung.baktimarsada.network.api

import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.network.dto.ApiResponseDto
import com.lampung.baktimarsada.network.dto.BackendAuthResponseDto
import com.lampung.baktimarsada.network.dto.BackendLoginRequestDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountRequestDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountResponseDto
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.FcmTokenRequestDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.WorshipTemplateDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BaktiApiService {
    @POST(AppConstants.ROUTE_AUTH_LOGIN)
    suspend fun login(@Body request: BackendLoginRequestDto): Response<ApiResponseDto<BackendAuthResponseDto>>

    @POST(AppConstants.ROUTE_AUTH_LOGOUT)
    suspend fun logout(@Header("Authorization") authorization: String): Response<ApiResponseDto<Unit?>>

    @POST(AppConstants.ROUTE_SYNC_FCM_TOKEN)
    suspend fun syncFcmToken(@Body request: FcmTokenRequestDto): Response<ApiResponseDto<Unit?>>

    @GET(AppConstants.ROUTE_EVENTS)
    suspend fun fetchEvents(@Query("sectorId") sectorId: String): Response<ApiResponseDto<List<EventDto>>>

    @POST(AppConstants.ROUTE_EVENTS)
    suspend fun saveEvent(@Body event: EventDto): Response<ApiResponseDto<EventDto>>

    @DELETE("${AppConstants.ROUTE_EVENTS}/{id}")
    suspend fun deleteEvent(@Path("id") eventId: String): Response<ApiResponseDto<Unit?>>

    @GET(AppConstants.ROUTE_WORSHIP_TEMPLATES)
    suspend fun fetchWorshipTemplates(@Query("sectorId") sectorId: String): Response<ApiResponseDto<List<WorshipTemplateDto>>>

    @POST(AppConstants.ROUTE_WORSHIP_TEMPLATES)
    suspend fun saveWorshipTemplate(@Body template: WorshipTemplateDto): Response<ApiResponseDto<WorshipTemplateDto>>

    @DELETE("${AppConstants.ROUTE_WORSHIP_TEMPLATES}/{id}")
    suspend fun deleteWorshipTemplate(@Path("id") templateId: String): Response<ApiResponseDto<Unit?>>

    @GET(AppConstants.ROUTE_MEMBERS)
    suspend fun fetchMembers(@Query("sectorId") sectorId: String): Response<ApiResponseDto<List<MemberDto>>>

    @POST(AppConstants.ROUTE_MEMBERS)
    suspend fun saveMember(@Body member: MemberDto): Response<ApiResponseDto<MemberDto>>

    @DELETE("${AppConstants.ROUTE_MEMBERS}/{id}")
    suspend fun deleteMember(@Path("id") memberId: String): Response<ApiResponseDto<Unit?>>

    @GET(AppConstants.ROUTE_FINANCE_REPORTS)
    suspend fun fetchFinanceReports(@Query("sectorId") sectorId: String): Response<ApiResponseDto<List<FinanceReportDto>>>

    @POST(AppConstants.ROUTE_FINANCE_REPORTS)
    suspend fun saveFinanceReport(@Body report: FinanceReportDto): Response<ApiResponseDto<FinanceReportDto>>

    @DELETE("${AppConstants.ROUTE_FINANCE_REPORTS}/{id}")
    suspend fun deleteFinanceReport(@Path("id") reportId: String): Response<ApiResponseDto<Unit?>>

    @GET(AppConstants.ROUTE_PAYMENT_OBLIGATIONS)
    suspend fun fetchPaymentObligations(@Query("sectorId") sectorId: String): Response<ApiResponseDto<List<PaymentObligationDto>>>

    @POST(AppConstants.ROUTE_PAYMENT_OBLIGATIONS)
    suspend fun savePaymentObligation(@Body obligation: PaymentObligationDto): Response<ApiResponseDto<PaymentObligationDto>>

    @DELETE("${AppConstants.ROUTE_PAYMENT_OBLIGATIONS}/{id}")
    suspend fun deletePaymentObligation(@Path("id") obligationId: String): Response<ApiResponseDto<Unit?>>

    @POST(AppConstants.ROUTE_USERS)
    suspend fun createUser(@Body request: CreateUserAccountRequestDto): Response<ApiResponseDto<CreateUserAccountResponseDto>>

    @GET(AppConstants.ROUTE_USERS)
    suspend fun fetchUsers(@Query("role") role: String): Response<ApiResponseDto<List<CreateUserAccountResponseDto>>>
}

// created by Mories Deo Hutapea, S.E.,S.Kom
