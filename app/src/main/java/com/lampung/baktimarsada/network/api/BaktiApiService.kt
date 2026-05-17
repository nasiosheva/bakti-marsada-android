package com.lampung.baktimarsada.network.api

import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.network.dto.FcmTokenRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BaktiApiService {
    @POST(AppConstants.ROUTE_SYNC_FCM_TOKEN)
    suspend fun syncFcmToken(@Body request: FcmTokenRequestDto): Response<Unit>
}
