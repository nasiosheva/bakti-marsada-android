package com.lampung.baktimarsada.datasource.simulate

import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.EventProgramItemDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountRequestDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountResponseDto
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
import com.lampung.baktimarsada.network.dto.WorshipTemplateDto
import com.lampung.baktimarsada.network.dto.WorshipTemplateItemDto
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

    private val accounts = mutableListOf(
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

    private val worshipTemplates = mutableListOf<WorshipTemplateDto>()

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
                sectorName = TenantRuntime.current.defaultSectorName,
                programItems = templateItemsForEvent("event-1", seedSectorTemplateItems("template-sector"))
            ),
            EventDto(
                id = "event-2",
                title = "Partangiangan Keluarga Muda",
                description = "Fokus pembinaan keluarga muda dan sharing pergumulan rumah tangga.",
                scheduledAt = "2026-05-23 18:30",
                location = "Aula Wijk Bethesda",
                sectorId = TenantRuntime.current.defaultSectorId,
                sectorName = TenantRuntime.current.defaultSectorName,
                programItems = templateItemsForEvent("event-2", seedFamilyTemplateItems("template-family"))
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

    private fun seedWorshipTemplates(): List<WorshipTemplateDto> {
        return listOf(
            WorshipTemplateDto(
                id = "template-sector",
                tenantId = TenantRuntime.current.tenantId,
                sectorId = TenantRuntime.current.defaultSectorId,
                title = "Partangiangan Sektor/Wijk",
                description = "Susunan umum partangiangan rutin sektor atau wijk.",
                items = seedSectorTemplateItems("template-sector")
            ),
            WorshipTemplateDto(
                id = "template-family",
                tenantId = TenantRuntime.current.tenantId,
                sectorId = TenantRuntime.current.defaultSectorId,
                title = "Partangiangan Keluarga",
                description = "Susunan ringkas untuk ibadah keluarga dan pembinaan rumah tangga.",
                items = seedFamilyTemplateItems("template-family")
            ),
            WorshipTemplateDto(
                id = "template-thanksgiving",
                tenantId = TenantRuntime.current.tenantId,
                sectorId = TenantRuntime.current.defaultSectorId,
                title = "Partangiangan Syukuran",
                description = "Susunan acara untuk ucapan syukur keluarga atau sektor.",
                items = seedThanksgivingTemplateItems("template-thanksgiving")
            )
        )
    }

    private fun seedSectorTemplateItems(templateId: String): List<WorshipTemplateItemDto> {
        return listOf(
            templateItem(templateId, 0, "Pembukaan", "Pelayan membuka ibadah dan mengajak jemaat memusatkan hati kepada Tuhan.", "Liturgis", "OPENING"),
            templateItem(templateId, 1, "Nyanyian Pembuka", "Pilih satu nyanyian pembuka yang sesuai dengan tema persekutuan.", "Song Leader", "SONG"),
            templateItem(templateId, 2, "Doa Pembuka", "Doa singkat untuk menyerahkan persekutuan, keluarga tuan rumah, dan seluruh warga sektor.", "Liturgis", "PRAYER"),
            templateItem(
                templateId = templateId,
                orderIndex = 3,
                title = "Pembacaan Alkitab",
                content = "Bacakan nas pilihan, lalu beri jeda singkat untuk perenungan pribadi.",
                leader = "Pembaca Alkitab",
                type = "SCRIPTURE",
                scriptureReference = "Mazmur 100:1-5",
                scriptureText = "Baca nas pilihan sesuai tema partangiangan."
            ),
            templateItem(templateId, 4, "Renungan Singkat", "Renungan diarahkan pada penguatan iman, kebersamaan sektor, dan pelayanan sehari-hari.", "Pembawa Renungan", "SERMON"),
            templateItem(templateId, 5, "Doa Syafaat", "Doakan keluarga, warga yang sakit, pelayanan gereja, dan pergumulan sektor.", "Liturgis", "PRAYER"),
            templateItem(templateId, 6, "Persembahan", "Jemaat memberi persembahan sebagai ungkapan syukur dan dukungan pelayanan sektor.", "Bendahara Sektor", "OFFERING"),
            templateItem(templateId, 7, "Pengumuman", "Sampaikan agenda sektor, informasi pelayanan, dan kewajiban yang perlu diperhatikan.", "Pengurus Sektor", "ANNOUNCEMENT"),
            templateItem(templateId, 8, "Doa Penutup", "Tutup ibadah dengan doa syukur dan permohonan penyertaan Tuhan.", "Liturgis", "CLOSING")
        )
    }

    private fun seedFamilyTemplateItems(templateId: String): List<WorshipTemplateItemDto> {
        return listOf(
            templateItem(templateId, 0, "Sapaan Keluarga", "Tuan rumah atau liturgis menyapa peserta dan menyampaikan pokok syukur/pergumulan.", "Tuan Rumah", "OPENING"),
            templateItem(templateId, 1, "Nyanyian", "Nyanyian dipilih yang mudah dinyanyikan bersama keluarga.", "Song Leader", "SONG"),
            templateItem(templateId, 2, "Doa", "Doa pembuka untuk keluarga, anak-anak, dan kehidupan rumah tangga.", "Liturgis", "PRAYER"),
            templateItem(templateId, 3, "Firman Tuhan", "Pembacaan nas dan renungan singkat yang dekat dengan kehidupan keluarga.", "Pembawa Renungan", "SERMON"),
            templateItem(templateId, 4, "Sharing dan Doa Syafaat", "Keluarga dapat menyampaikan pokok doa sebelum didoakan bersama.", "Liturgis", "PRAYER"),
            templateItem(templateId, 5, "Penutup", "Akhiri dengan doa dan salam persekutuan.", "Liturgis", "CLOSING")
        )
    }

    private fun seedThanksgivingTemplateItems(templateId: String): List<WorshipTemplateItemDto> {
        return listOf(
            templateItem(templateId, 0, "Pembukaan Syukur", "Liturgis membuka acara dan menyebut pokok ucapan syukur secara ringkas.", "Liturgis", "OPENING"),
            templateItem(templateId, 1, "Nyanyian Syukur", "Nyanyikan lagu yang menekankan ucapan syukur dan penyertaan Tuhan.", "Song Leader", "SONG"),
            templateItem(templateId, 2, "Doa Syukur", "Doa berfokus pada rasa terima kasih dan penyerahan rencana keluarga ke depan.", "Liturgis", "PRAYER"),
            templateItem(
                templateId = templateId,
                orderIndex = 3,
                title = "Pembacaan Firman",
                content = "Bacaan dan renungan menguatkan keluarga untuk hidup dalam syukur.",
                leader = "Pembawa Renungan",
                type = "SCRIPTURE",
                scriptureReference = "1 Tesalonika 5:16-18",
                scriptureText = "Baca nas pilihan tentang ucapan syukur."
            ),
            templateItem(templateId, 4, "Ucapan Syukur Keluarga", "Perwakilan keluarga dapat menyampaikan kesaksian singkat.", "Tuan Rumah", "ANNOUNCEMENT"),
            templateItem(templateId, 5, "Doa Penutup", "Tutup acara dengan doa berkat bagi keluarga dan jemaat yang hadir.", "Liturgis", "CLOSING")
        )
    }

    private fun templateItem(
        templateId: String,
        orderIndex: Int,
        title: String,
        content: String,
        leader: String,
        type: String,
        scriptureReference: String = "",
        scriptureText: String = "",
        note: String = ""
    ): WorshipTemplateItemDto {
        return WorshipTemplateItemDto(
            id = "$templateId-item-$orderIndex",
            templateId = templateId,
            orderIndex = orderIndex,
            title = title,
            content = content,
            leader = leader,
            type = type,
            scriptureReference = scriptureReference,
            scriptureText = scriptureText,
            note = note
        )
    }

    private fun templateItemsForEvent(eventId: String, items: List<WorshipTemplateItemDto>): List<EventProgramItemDto> {
        return items.map { item ->
            EventProgramItemDto(
                id = "$eventId-program-${item.orderIndex}",
                eventId = eventId,
                orderIndex = item.orderIndex,
                title = item.title,
                content = item.content,
                leader = item.leader,
                type = item.type,
                scriptureReference = item.scriptureReference,
                scriptureText = item.scriptureText,
                note = item.note
            )
        }
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
        val normalizedIdentifier = request.identifier.trim()
        val account = accounts.firstOrNull {
            val isEmailMatch = it.identifier.equals(normalizedIdentifier, ignoreCase = true)
            val isAdminUsernameMatch =
                it.role == "ADMIN" &&
                    it.identifier.substringBefore("@").equals(normalizedIdentifier, ignoreCase = true)
            (isEmailMatch || isAdminUsernameMatch) && it.password == request.password
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

    override suspend fun fetchWorshipTemplates(sectorId: String): List<WorshipTemplateDto> {
        return worshipTemplates.filter { it.sectorId == null || it.sectorId == sectorId }.sortedBy { it.title }
    }

    override suspend fun saveWorshipTemplate(template: WorshipTemplateDto): WorshipTemplateDto {
        val resolved = template.withIdIfNeeded(prefix = "template")
        worshipTemplates.removeAll { it.id == resolved.id }
        worshipTemplates.add(resolved)
        return resolved
    }

    override suspend fun deleteWorshipTemplate(templateId: String) {
        worshipTemplates.removeAll { it.id == templateId }
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

    override suspend fun createUserAccount(request: CreateUserAccountRequestDto): CreateUserAccountResponseDto {
        val username = request.username.trim().lowercase()
        val email = request.email.trim().lowercase()
        val role = request.role.trim().uppercase().ifBlank { "JEMAAT" }
        val fullName = request.fullName.trim()
        val password = request.password

        if (username.length < 3) {
            throw IllegalArgumentException("Username must be at least 3 characters")
        }
        if (!email.contains("@")) {
            throw IllegalArgumentException("Invalid email")
        }
        if (password.length < 8) {
            throw IllegalArgumentException("Password must be at least 8 characters")
        }
        if (fullName.isBlank()) {
            throw IllegalArgumentException("Full name is required")
        }
        if (accounts.any { it.identifier.equals(email, ignoreCase = true) }) {
            throw IllegalArgumentException("Email already exists")
        }
        if (accounts.any { it.identifier.substringBefore("@").equals(username, ignoreCase = true) }) {
            throw IllegalArgumentException("Username already exists")
        }

        val userId = "user-${UUID.randomUUID()}"
        accounts.add(
            SampleAccount(
                identifier = email,
                password = password,
                userId = userId,
                displayName = fullName,
                role = if (role == "ADMIN") "ADMIN" else "JEMAAT",
                sectorId = TenantRuntime.current.defaultSectorId,
                sectorName = TenantRuntime.current.defaultSectorName
            )
        )

        return CreateUserAccountResponseDto(
            id = userId,
            username = username,
            email = email,
            role = role,
            fullName = fullName
        )
    }

    override suspend fun fetchUsersByRole(role: String): List<CreateUserAccountResponseDto> {
        val normalizedRole = role.trim().uppercase()
        return accounts
            .filter { it.role.uppercase() == normalizedRole }
            .map {
                CreateUserAccountResponseDto(
                    id = it.userId,
                    username = it.identifier.substringBefore("@"),
                    email = it.identifier,
                    role = it.role,
                    fullName = it.displayName
                )
            }
    }

    override suspend fun resetSimulationData() {
        resetDataInternal()
    }

    private fun resetDataInternal() {
        accounts.clear()
        accounts.addAll(
            listOf(
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
        )
        events.clear()
        events.addAll(seedEvents())
        worshipTemplates.clear()
        worshipTemplates.addAll(seedWorshipTemplates())
        members.clear()
        members.addAll(seedMembers())
        financeReports.clear()
        financeReports.addAll(seedFinanceReports())
        paymentObligations.clear()
        paymentObligations.addAll(seedPaymentObligations())
    }

    private fun EventDto.withIdIfNeeded(prefix: String): EventDto {
        val resolvedId = id.ifBlank { "$prefix-${UUID.randomUUID()}" }
        return copy(
            id = resolvedId,
            programItems = programItems.mapIndexed { index, item ->
                item.copy(
                    id = item.id.ifBlank { "$resolvedId-program-$index" },
                    eventId = resolvedId,
                    orderIndex = index
                )
            }
        )
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

    private fun WorshipTemplateDto.withIdIfNeeded(prefix: String): WorshipTemplateDto {
        if (id.isNotBlank()) return this
        val resolvedId = "$prefix-${UUID.randomUUID()}"
        return copy(
            id = resolvedId,
            items = items.mapIndexed { index, item ->
                item.copy(
                    id = item.id.ifBlank { "$resolvedId-item-$index" },
                    templateId = resolvedId,
                    orderIndex = index
                )
            }
        )
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
