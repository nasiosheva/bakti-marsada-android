package com.lampung.baktimarsada.datasource.simulate

import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SimulateAppRemoteDataSource @Inject constructor() : AppRemoteDataSource {

    private val accounts = listOf(
        SampleAccount(
            identifier = TenantRuntime.current.sampleAdminIdentifier,
            password = TenantRuntime.current.sampleAdminPassword,
            userId = "admin-1",
            displayName = "Admin Sektor 1",
            role = "ADMIN",
            sectorId = TenantRuntime.current.defaultSectorId,
            sectorName = TenantRuntime.current.defaultSectorName
        ),
        SampleAccount(
            identifier = TenantRuntime.current.sampleJemaatIdentifier,
            password = TenantRuntime.current.sampleJemaatPassword,
            userId = "jemaat-1",
            displayName = "Jemaat Sektor 1",
            role = "JEMAAT",
            sectorId = TenantRuntime.current.defaultSectorId,
            sectorName = TenantRuntime.current.defaultSectorName
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
                sectorId = TenantRuntime.current.defaultSectorId,
                sectorName = TenantRuntime.current.defaultSectorName
            ),
            EventDto(
                id = "event-2",
                title = "Partangiangan Keluarga Muda",
                description = "Fokus pembinaan keluarga muda dan sharing pergumulan rumah tangga.",
                scheduledAt = "2026-05-23 18:30",
                location = "Aula Wijk Bethesda",
                sectorId = TenantRuntime.current.defaultSectorId,
                sectorName = TenantRuntime.current.defaultSectorName
            ),
            EventDto(
                id = "event-3",
                title = "Doa Syafaat Akhir Bulan",
                description = "Pertemuan doa untuk kebutuhan warga sektor dan pelayanan jemaat.",
                scheduledAt = "2026-05-30 19:00",
                location = "Rumah Keluarga Hutagalung",
                sectorId = TenantRuntime.current.defaultSectorId,
                sectorName = TenantRuntime.current.defaultSectorName
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
                sectorId = TenantRuntime.current.defaultSectorId,
                sectorName = TenantRuntime.current.defaultSectorName
            ),
            MemberDto(
                id = "member-2",
                fullName = "S. Sihombing",
                familyGroup = "Keluarga Sihombing/Situmorang",
                phoneNumber = "081298765432",
                address = "Jl. Kenanga No. 4",
                roleInSector = "Sekretaris Sektor",
                sectorId = TenantRuntime.current.defaultSectorId,
                sectorName = TenantRuntime.current.defaultSectorName
            ),
            MemberDto(
                id = "member-3",
                fullName = "R. Naibaho",
                familyGroup = "Keluarga Naibaho/Hutapea",
                phoneNumber = "082212345678",
                address = "Jl. Merdeka No. 11",
                roleInSector = "Jemaat",
                sectorId = TenantRuntime.current.defaultSectorId,
                sectorName = TenantRuntime.current.defaultSectorName
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
                amount = 2_250_000,
                isVisibleToJemaat = true,
                sectorId = TenantRuntime.current.defaultSectorId,
                sectorName = TenantRuntime.current.defaultSectorName
            ),
            FinanceReportDto(
                id = "finance-2",
                title = "Dana Diakonia Internal",
                description = "Cadangan bantuan internal keluarga sektor yang membutuhkan.",
                periodLabel = "Triwulan II 2026",
                amount = 1_500_000,
                isVisibleToJemaat = false,
                sectorId = TenantRuntime.current.defaultSectorId,
                sectorName = TenantRuntime.current.defaultSectorName
            ),
            FinanceReportDto(
                id = "finance-3",
                title = "Dana Operasional Wijk",
                description = "Biaya cetak warta, transport pelayanan, dan alat tulis sektor.",
                periodLabel = "April 2026",
                amount = 875_000,
                isVisibleToJemaat = true,
                sectorId = TenantRuntime.current.defaultSectorId,
                sectorName = TenantRuntime.current.defaultSectorName
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
                amount = 50_000,
                dueDate = "2026-05-25",
                status = "PAID",
                sectorId = TenantRuntime.current.defaultSectorId,
                sectorName = TenantRuntime.current.defaultSectorName
            ),
            PaymentObligationDto(
                id = "payment-2",
                memberId = "member-2",
                memberName = "S. Sihombing",
                title = "Iuran Paskah Wijk",
                description = "Kontribusi kegiatan paskah wijk untuk keluarga sektor.",
                amount = 100_000,
                dueDate = "2026-05-19",
                status = "UNPAID",
                sectorId = TenantRuntime.current.defaultSectorId,
                sectorName = TenantRuntime.current.defaultSectorName
            ),
            PaymentObligationDto(
                id = "payment-3",
                memberId = "member-3",
                memberName = "R. Naibaho",
                title = "Partisipasi Retreat Sektor",
                description = "Kewajiban pembayaran retreat pembinaan iman sektor.",
                amount = 250_000,
                dueDate = "2026-05-10",
                status = "OVERDUE",
                sectorId = TenantRuntime.current.defaultSectorId,
                sectorName = TenantRuntime.current.defaultSectorName
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
            tenantId = TenantRuntime.current.tenantId,
            tenantName = TenantRuntime.current.tenantName,
            subTenantId = TenantRuntime.current.subTenantId,
            subTenantName = TenantRuntime.current.subTenantName,
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

@Module
@InstallIn(SingletonComponent::class)
abstract class SimulateDataSourceModule {

    @Binds
    @Named(AppConstants.QUALIFIER_SIMULATE_SOURCE)
    abstract fun bindSimulateDataSource(impl: SimulateAppRemoteDataSource): AppRemoteDataSource
}
