package com.lampung.baktimarsada.data.remote

import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.BackendLoginRequestDto
import com.lampung.baktimarsada.network.dto.FcmTokenRequestDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
import com.lampung.baktimarsada.network.api.BaktiApiService
import retrofit2.Response
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

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
    suspend fun resetSimulationData()
}

@Singleton
class SimulateAppRemoteDataSource @Inject constructor() : AppRemoteDataSource {

    private val accounts = listOf(
        SampleAccount(
            identifier = AppConstants.SAMPLE_ADMIN_IDENTIFIER,
            password = AppConstants.SAMPLE_ADMIN_PASSWORD,
            userId = "admin-1",
            displayName = "Admin Sektor 1",
            role = "ADMIN",
            sectorId = AppConstants.DEFAULT_SECTOR_ID,
            sectorName = AppConstants.DEFAULT_SECTOR_NAME
        ),
        SampleAccount(
            identifier = AppConstants.SAMPLE_JEMAAT_IDENTIFIER,
            password = AppConstants.SAMPLE_JEMAAT_PASSWORD,
            userId = "jemaat-1",
            displayName = "Jemaat Sektor 1",
            role = "JEMAAT",
            sectorId = AppConstants.DEFAULT_SECTOR_ID,
            sectorName = AppConstants.DEFAULT_SECTOR_NAME
        )
    )

    private val sessionTokens = mutableSetOf<String>()

    private val events = mutableListOf<EventDto>()

    private val members = mutableListOf<MemberDto>()

    private val financeReports = mutableListOf<FinanceReportDto>()

    private val paymentObligations = mutableListOf<PaymentObligationDto>()

    init {
        resetDataInternal()
    }

    private fun seedEvents(): List<EventDto> {
        return listOf(
            EventDto(
            id = "event-1",
            title = "Partangiangan Rabu Malam",
            description = "Ibadah rutin sektor dengan renungan dan doa syafaat.",
            scheduledAt = "2026-05-20 19:30",
            location = "Rumah Keluarga Sinaga",
            sectorId = AppConstants.DEFAULT_SECTOR_ID,
            sectorName = AppConstants.DEFAULT_SECTOR_NAME
        ),
        EventDto(
            id = "event-2",
            title = "Partangiangan Keluarga Muda",
            description = "Fokus pembinaan keluarga muda dan sharing pergumulan rumah tangga.",
            scheduledAt = "2026-05-23 18:30",
            location = "Aula Wijk Bethesda",
            sectorId = AppConstants.DEFAULT_SECTOR_ID,
            sectorName = AppConstants.DEFAULT_SECTOR_NAME
        ),
        EventDto(
            id = "event-3",
            title = "Doa Syafaat Akhir Bulan",
            description = "Pertemuan doa untuk kebutuhan warga sektor dan pelayanan jemaat.",
            scheduledAt = "2026-05-30 19:00",
            location = "Rumah Keluarga Hutagalung",
            sectorId = AppConstants.DEFAULT_SECTOR_ID,
            sectorName = AppConstants.DEFAULT_SECTOR_NAME
        )
        )
    }

    private fun seedMembers(): List<MemberDto> {
        return listOf(
            MemberDto(
            id = "member-1",
            fullName = "P. Simanjuntak",
            familyGroup = "Keluarga Simanjuntak/Butarbutar",
            phoneNumber = "081234567890",
            address = "Jl. Melati No. 7",
            roleInSector = "Penatua Pendamping",
            sectorId = AppConstants.DEFAULT_SECTOR_ID,
            sectorName = AppConstants.DEFAULT_SECTOR_NAME
        ),
        MemberDto(
            id = "member-2",
            fullName = "S. Sihombing",
            familyGroup = "Keluarga Sihombing/Situmorang",
            phoneNumber = "081298765432",
            address = "Jl. Kenanga No. 4",
            roleInSector = "Sekretaris Sektor",
            sectorId = AppConstants.DEFAULT_SECTOR_ID,
            sectorName = AppConstants.DEFAULT_SECTOR_NAME
        ),
        MemberDto(
            id = "member-3",
            fullName = "R. Naibaho",
            familyGroup = "Keluarga Naibaho/Hutapea",
            phoneNumber = "082212345678",
            address = "Jl. Merdeka No. 11",
            roleInSector = "Jemaat",
            sectorId = AppConstants.DEFAULT_SECTOR_ID,
            sectorName = AppConstants.DEFAULT_SECTOR_NAME
        )
        )
    }

    private fun seedFinanceReports(): List<FinanceReportDto> {
        return listOf(
            FinanceReportDto(
            id = "finance-1",
            title = "Kas Partangiangan Mei",
            description = "Pemasukan persembahan dan pengeluaran konsumsi selama Mei 2026.",
            periodLabel = "Mei 2026",
            amount = 2250000,
            isVisibleToJemaat = true,
            sectorId = AppConstants.DEFAULT_SECTOR_ID,
            sectorName = AppConstants.DEFAULT_SECTOR_NAME
        ),
        FinanceReportDto(
            id = "finance-2",
            title = "Dana Diakonia Internal",
            description = "Cadangan bantuan internal keluarga sektor yang membutuhkan.",
            periodLabel = "Triwulan II 2026",
            amount = 1500000,
            isVisibleToJemaat = false,
            sectorId = AppConstants.DEFAULT_SECTOR_ID,
            sectorName = AppConstants.DEFAULT_SECTOR_NAME
        ),
        FinanceReportDto(
            id = "finance-3",
            title = "Dana Operasional Wijk",
            description = "Biaya cetak warta, transport pelayanan, dan alat tulis sektor.",
            periodLabel = "April 2026",
            amount = 875000,
            isVisibleToJemaat = true,
            sectorId = AppConstants.DEFAULT_SECTOR_ID,
            sectorName = AppConstants.DEFAULT_SECTOR_NAME
        )
        )
    }

    private fun seedPaymentObligations(): List<PaymentObligationDto> {
        return listOf(
            PaymentObligationDto(
            id = "payment-1",
            memberId = "member-1",
            memberName = "P. Simanjuntak",
            title = "Iuran Sektor Mei",
            description = "Iuran bulanan sektor untuk operasional dan konsumsi partangiangan.",
            amount = 50000,
            dueDate = "2026-05-25",
            status = "PAID",
            sectorId = AppConstants.DEFAULT_SECTOR_ID,
            sectorName = AppConstants.DEFAULT_SECTOR_NAME
        ),
        PaymentObligationDto(
            id = "payment-2",
            memberId = "member-2",
            memberName = "S. Sihombing",
            title = "Iuran Paskah Wijk",
            description = "Kontribusi kegiatan paskah wijk untuk keluarga sektor.",
            amount = 100000,
            dueDate = "2026-05-19",
            status = "UNPAID",
            sectorId = AppConstants.DEFAULT_SECTOR_ID,
            sectorName = AppConstants.DEFAULT_SECTOR_NAME
        ),
        PaymentObligationDto(
            id = "payment-3",
            memberId = "member-3",
            memberName = "R. Naibaho",
            title = "Partisipasi Retreat Sektor",
            description = "Kewajiban pembayaran retreat pembinaan iman sektor.",
            amount = 250000,
            dueDate = "2026-05-10",
            status = "OVERDUE",
            sectorId = AppConstants.DEFAULT_SECTOR_ID,
            sectorName = AppConstants.DEFAULT_SECTOR_NAME
        )
        )
    }

    override suspend fun login(request: LoginRequestDto): SessionResponseDto {
        val account = accounts.firstOrNull {
            it.identifier.equals(request.identifier.trim(), ignoreCase = true) &&
                it.password == request.password
        } ?: throw IllegalArgumentException("Invalid credentials")

        val token = "token-${UUID.randomUUID()}"
        sessionTokens.add(token)
        return SessionResponseDto(
            authToken = token,
            userId = account.userId,
            displayName = account.displayName,
            role = account.role,
            sectorId = account.sectorId,
            sectorName = account.sectorName
        )
    }

    override suspend fun logout(token: String) {
        sessionTokens.remove(token)
    }

    override suspend fun syncFcmToken(token: String) = Unit

    override suspend fun fetchEvents(sectorId: String): List<EventDto> {
        return events.filter { it.sectorId == sectorId }.sortedBy { it.scheduledAt }
    }

    override suspend fun saveEvent(event: EventDto): EventDto {
        val resolved = event.withIdIfNeeded(prefix = "event")
        events.removeAll { it.id == resolved.id }
        events.add(resolved)
        return resolved
    }

    override suspend fun deleteEvent(eventId: String) {
        events.removeAll { it.id == eventId }
    }

    override suspend fun fetchMembers(sectorId: String): List<MemberDto> {
        return members.filter { it.sectorId == sectorId }.sortedBy { it.fullName }
    }

    override suspend fun saveMember(member: MemberDto): MemberDto {
        val resolved = member.withIdIfNeeded(prefix = "member")
        members.removeAll { it.id == resolved.id }
        members.add(resolved)
        return resolved
    }

    override suspend fun deleteMember(memberId: String) {
        members.removeAll { it.id == memberId }
        paymentObligations.removeAll { it.memberId == memberId }
    }

    override suspend fun fetchFinanceReports(sectorId: String): List<FinanceReportDto> {
        return financeReports.filter { it.sectorId == sectorId }.sortedByDescending { it.periodLabel }
    }

    override suspend fun saveFinanceReport(report: FinanceReportDto): FinanceReportDto {
        val resolved = report.withIdIfNeeded(prefix = "finance")
        financeReports.removeAll { it.id == resolved.id }
        financeReports.add(resolved)
        return resolved
    }

    override suspend fun deleteFinanceReport(reportId: String) {
        financeReports.removeAll { it.id == reportId }
    }

    override suspend fun fetchPaymentObligations(sectorId: String): List<PaymentObligationDto> {
        return paymentObligations.filter { it.sectorId == sectorId }.sortedBy { it.dueDate }
    }

    override suspend fun savePaymentObligation(obligation: PaymentObligationDto): PaymentObligationDto {
        val resolved = obligation.withIdIfNeeded(prefix = "payment")
        paymentObligations.removeAll { it.id == resolved.id }
        paymentObligations.add(resolved)
        return resolved
    }

    override suspend fun deletePaymentObligation(obligationId: String) {
        paymentObligations.removeAll { it.id == obligationId }
    }

    override suspend fun resetSimulationData() {
        resetDataInternal()
    }

    private fun resetDataInternal() {
        events.clear()
        events.addAll(seedEvents())
        members.clear()
        members.addAll(seedMembers())
        financeReports.clear()
        financeReports.addAll(seedFinanceReports())
        paymentObligations.clear()
        paymentObligations.addAll(seedPaymentObligations())
    }

    private fun EventDto.withIdIfNeeded(prefix: String): EventDto {
        if (id.isNotBlank()) return this
        return copy(id = "$prefix-${UUID.randomUUID()}")
    }

    private fun MemberDto.withIdIfNeeded(prefix: String): MemberDto {
        if (id.isNotBlank()) return this
        return copy(id = "$prefix-${UUID.randomUUID()}")
    }

    private fun FinanceReportDto.withIdIfNeeded(prefix: String): FinanceReportDto {
        if (id.isNotBlank()) return this
        return copy(id = "$prefix-${UUID.randomUUID()}")
    }

    private fun PaymentObligationDto.withIdIfNeeded(prefix: String): PaymentObligationDto {
        if (id.isNotBlank()) return this
        return copy(id = "$prefix-${UUID.randomUUID()}")
    }

    private data class SampleAccount(
        val identifier: String,
        val password: String,
        val userId: String,
        val displayName: String,
        val role: String,
        val sectorId: String,
        val sectorName: String
    )
}

@Singleton
class BackendAppRemoteDataSource @Inject constructor(
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
            sectorId = body.sectorId ?: AppConstants.DEFAULT_SECTOR_ID,
            sectorName = body.sectorName ?: AppConstants.DEFAULT_SECTOR_NAME
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
