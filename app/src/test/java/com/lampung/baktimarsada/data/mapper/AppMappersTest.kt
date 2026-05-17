package com.lampung.baktimarsada.data.mapper

import com.lampung.baktimarsada.db.entity.PaymentObligationEntity
import com.lampung.baktimarsada.domain.model.PaymentStatus
import com.lampung.baktimarsada.domain.model.UserRole
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
}

// created by Mories Deo Hutapea, S.E.,S.Kom
