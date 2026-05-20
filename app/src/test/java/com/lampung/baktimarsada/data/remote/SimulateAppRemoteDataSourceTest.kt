package com.lampung.baktimarsada.data.remote

import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.datasource.simulate.SimulateAppRemoteDataSource
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimulateAppRemoteDataSourceTest {
    private val tenant = TenantRuntime.current
    private val tenantId = tenant.tenantId

    @Test
    fun `resetSimulationData restores seeded records after mutations`() = runTest {
        val dataSource = SimulateAppRemoteDataSource()
        val session = dataSource.login(
            LoginRequestDto(
                identifier = tenant.sampleAdminIdentifier,
                password = tenant.sampleAdminPassword
            )
        )

        val sectorId = session.sectorId
        val initialEvents = dataSource.fetchEvents(tenantId, sectorId).size
        val initialMembers = dataSource.fetchMembers(tenantId, sectorId).size
        val initialReports = dataSource.fetchFinanceReports(tenantId, sectorId).size
        val initialPayments = dataSource.fetchPaymentObligations(tenantId, sectorId).size

        val firstEventId = dataSource.fetchEvents(tenantId, sectorId).first().id
        val firstMemberId = dataSource.fetchMembers(tenantId, sectorId).first().id
        val firstReportId = dataSource.fetchFinanceReports(tenantId, sectorId).first().id
        val firstPaymentId = dataSource.fetchPaymentObligations(tenantId, sectorId).first().id

        dataSource.deleteEvent(tenantId, firstEventId)
        dataSource.deleteMember(tenantId, firstMemberId)
        dataSource.deleteFinanceReport(tenantId, firstReportId)
        dataSource.deletePaymentObligation(tenantId, firstPaymentId)

        assertTrue(dataSource.fetchEvents(tenantId, sectorId).size < initialEvents)
        assertTrue(dataSource.fetchMembers(tenantId, sectorId).size < initialMembers)
        assertTrue(dataSource.fetchFinanceReports(tenantId, sectorId).size < initialReports)
        assertTrue(dataSource.fetchPaymentObligations(tenantId, sectorId).size < initialPayments)

        dataSource.resetSimulationData()

        assertEquals(initialEvents, dataSource.fetchEvents(tenantId, sectorId).size)
        assertEquals(initialMembers, dataSource.fetchMembers(tenantId, sectorId).size)
        assertEquals(initialReports, dataSource.fetchFinanceReports(tenantId, sectorId).size)
        assertEquals(initialPayments, dataSource.fetchPaymentObligations(tenantId, sectorId).size)
    }

    @Test
    fun `seeded records are tagged with current tenantId`() = runTest {
        val dataSource = SimulateAppRemoteDataSource()
        val sectorId = tenant.defaultSectorId

        dataSource.fetchEvents(tenantId, sectorId).forEach { event ->
            assertEquals(tenantId, event.tenantId)
        }
        dataSource.fetchMembers(tenantId, sectorId).forEach { member ->
            assertEquals(tenantId, member.tenantId)
        }
        dataSource.fetchFinanceReports(tenantId, sectorId).forEach { report ->
            assertEquals(tenantId, report.tenantId)
        }
        dataSource.fetchPaymentObligations(tenantId, sectorId).forEach { payment ->
            assertEquals(tenantId, payment.tenantId)
        }
    }

    @Test
    fun `fetch with foreign tenantId returns empty even when sectorId matches`() = runTest {
        val dataSource = SimulateAppRemoteDataSource()
        val sectorId = tenant.defaultSectorId
        val foreignTenantId = "other-church-tenant"

        // Sanity check: real tenant sees records
        assertTrue(dataSource.fetchEvents(tenantId, sectorId).isNotEmpty())
        assertTrue(dataSource.fetchMembers(tenantId, sectorId).isNotEmpty())

        // Foreign tenant should see nothing for the same sector
        assertTrue(dataSource.fetchEvents(foreignTenantId, sectorId).isEmpty())
        assertTrue(dataSource.fetchMembers(foreignTenantId, sectorId).isEmpty())
        assertTrue(dataSource.fetchFinanceReports(foreignTenantId, sectorId).isEmpty())
        assertTrue(dataSource.fetchPaymentObligations(foreignTenantId, sectorId).isEmpty())
    }

    @Test
    fun `cross-tenant write does not leak into other tenant fetches`() = runTest {
        val dataSource = SimulateAppRemoteDataSource()
        val sectorId = tenant.defaultSectorId
        val foreignTenantId = "other-church-tenant"

        // Tenant A creates a record
        val savedByA = dataSource.saveMember(
            tenantId = tenantId,
            member = MemberDto(
                id = "",
                fullName = "Anggota Tenant A",
                familyGroup = "Keluarga A",
                phoneNumber = "0811",
                address = "Alamat A",
                roleInSector = "Anggota",
                sectorId = sectorId,
                sectorName = tenant.defaultSectorName
            )
        )
        assertEquals(tenantId, savedByA.tenantId)

        // Tenant B creates a record on the same sectorId (worst-case shared sector)
        val savedByB = dataSource.saveMember(
            tenantId = foreignTenantId,
            member = MemberDto(
                id = "",
                fullName = "Anggota Tenant B",
                familyGroup = "Keluarga B",
                phoneNumber = "0822",
                address = "Alamat B",
                roleInSector = "Anggota",
                sectorId = sectorId,
                sectorName = "Sektor B"
            )
        )
        assertEquals(foreignTenantId, savedByB.tenantId)

        val aResults = dataSource.fetchMembers(tenantId, sectorId)
        val bResults = dataSource.fetchMembers(foreignTenantId, sectorId)

        // Tenant A sees its own record but not B's
        assertTrue(aResults.any { it.id == savedByA.id })
        assertFalse(aResults.any { it.id == savedByB.id })

        // Tenant B sees its own record but not A's
        assertTrue(bResults.any { it.id == savedByB.id })
        assertFalse(bResults.any { it.id == savedByA.id })
    }

    @Test
    fun `delete from foreign tenant cannot remove other tenant's records`() = runTest {
        val dataSource = SimulateAppRemoteDataSource()
        val sectorId = tenant.defaultSectorId
        val foreignTenantId = "other-church-tenant"

        val targetEvent = dataSource.fetchEvents(tenantId, sectorId).first()

        // Foreign tenant tries to delete A's event
        dataSource.deleteEvent(foreignTenantId, targetEvent.id)

        val afterDelete = dataSource.fetchEvents(tenantId, sectorId)
        assertTrue(
            "Tenant A's event must survive a foreign tenant's delete attempt",
            afterDelete.any { it.id == targetEvent.id }
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
