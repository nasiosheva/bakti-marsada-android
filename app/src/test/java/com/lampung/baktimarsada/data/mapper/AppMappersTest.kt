package com.lampung.baktimarsada.data.mapper

import com.lampung.baktimarsada.db.entity.PaymentObligationEntity
import com.lampung.baktimarsada.domain.model.EventProgramItem
import com.lampung.baktimarsada.domain.model.PaymentStatus
import com.lampung.baktimarsada.domain.model.ProgramItemType
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.domain.model.WorshipTemplate
import com.lampung.baktimarsada.domain.model.WorshipTemplateItem
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.EventProgramItemDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AppMappersTest {

    @Test
    fun `session dto maps role and sector context`() {
        val dto = SessionResponseDto(
            authToken = "token-1",
            userId = "user-1",
            displayName = "Admin Sektor",
            role = UserRole.ADMIN.name,
            tenantId = "hkbp",
            tenantName = "HKBP",
            subTenantId = "hkbp-kedaton",
            subTenantName = "HKBP Kedaton",
            sectorId = "wijk-1",
            sectorName = "Wijk Marturia"
        )

        val domain = dto.toDomain()

        assertEquals(UserRole.ADMIN, domain.role)
        assertEquals("hkbp", domain.tenantContext.tenantId)
        assertEquals("HKBP Kedaton", domain.tenantContext.subTenantName)
        assertEquals("wijk-1", domain.sectorContext.sectorId)
        assertEquals("Wijk Marturia", domain.sectorContext.sectorName)
    }

    @Test
    fun `finance dto preserves jemaat visibility when mapped to entity and domain`() {
        val dto = FinanceReportDto(
            id = "finance-1",
            title = "Kas Mingguan",
            description = "Pemasukan dan pengeluaran partangiangan",
            periodLabel = "Mei 2026",
            amount = 750000,
            isVisibleToJemaat = false,
            sectorId = "wijk-1",
            sectorName = "Wijk Marturia"
        )

        val domain = dto.toEntity().toDomain()

        assertFalse(domain.isVisibleToJemaat)
        assertEquals("wijk-1", domain.sectorId)
        assertEquals("Wijk Marturia", domain.sectorName)
    }

    @Test
    fun `payment entity maps status string to global payment status enum`() {
        val entity = PaymentObligationEntity(
            id = "payment-1",
            memberId = "member-1",
            memberName = "Ama Situmorang",
            title = "Iuran Kas Sektor",
            description = "Kewajiban bulanan",
            amount = 50000,
            dueDate = "2026-05-31",
            status = PaymentStatus.OVERDUE.name,
            sectorId = "wijk-1",
            sectorName = "Wijk Marturia"
        )

        val domain = entity.toDomain()

        assertEquals(PaymentStatus.OVERDUE, domain.status)
        assertEquals("wijk-1", domain.sectorId)
    }

    @Test
    fun `event dto preserves ordered program items through entity and domain mapping`() {
        val dto = EventDto(
            id = "event-1",
            title = "Partangiangan",
            description = "Rutin",
            scheduledAt = "2026-05-20 19:30",
            location = "Rumah Jemaat",
            sectorId = "wijk-1",
            sectorName = "Wijk Marturia",
            programItems = listOf(
                EventProgramItemDto("item-2", "event-1", 1, "Doa", "Doa bersama", "Liturgis", "PRAYER"),
                EventProgramItemDto(
                    id = "item-1",
                    eventId = "event-1",
                    orderIndex = 0,
                    title = "Pembacaan Alkitab",
                    content = "Isi fallback",
                    leader = "Liturgis",
                    type = "SCRIPTURE",
                    scriptureReference = "Mazmur 100:1-5",
                    scriptureText = "Isi ayat"
                )
            )
        )

        val eventEntity = dto.toEntity()
        val programItems = dto.programItems.map { it.toEntity(dto.id).toDomain() }
        val domain = eventEntity.toDomain(programItems)

        assertEquals("Pembacaan Alkitab", domain.programItems.first().title)
        assertEquals("Mazmur 100:1-5", domain.programItems.first().scriptureReference)
        assertEquals("Isi ayat", domain.programItems.first().scriptureText)
        assertEquals(ProgramItemType.PRAYER, domain.programItems.last().type)
    }

    @Test
    fun `worship template maps nested items to dto`() {
        val template = WorshipTemplate(
            id = "template-1",
            tenantId = "tenant-1",
            sectorId = "wijk-1",
            title = "Partangiangan Sektor",
            description = "Template rutin",
            items = listOf(
                WorshipTemplateItem(
                    id = "template-item-1",
                    templateId = "template-1",
                    orderIndex = 0,
                    title = "Renungan",
                    content = "Renungan singkat",
                    leader = "Pelayan",
                    type = ProgramItemType.SERMON,
                    note = "Catatan template"
                )
            )
        )

        val dto = template.toDto()

        assertEquals("template-1", dto.items.first().templateId)
        assertEquals("SERMON", dto.items.first().type)
        assertEquals("Catatan template", dto.items.first().note)
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
