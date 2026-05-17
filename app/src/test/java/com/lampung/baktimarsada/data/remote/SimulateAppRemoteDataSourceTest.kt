package com.lampung.baktimarsada.data.remote

import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.datasource.simulate.SimulateAppRemoteDataSource
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimulateAppRemoteDataSourceTest {
    private val tenant = TenantRuntime.current

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
        val initialEvents = dataSource.fetchEvents(sectorId).size
        val initialMembers = dataSource.fetchMembers(sectorId).size
        val initialReports = dataSource.fetchFinanceReports(sectorId).size
        val initialPayments = dataSource.fetchPaymentObligations(sectorId).size

        val firstEventId = dataSource.fetchEvents(sectorId).first().id
        val firstMemberId = dataSource.fetchMembers(sectorId).first().id
        val firstReportId = dataSource.fetchFinanceReports(sectorId).first().id
        val firstPaymentId = dataSource.fetchPaymentObligations(sectorId).first().id

        dataSource.deleteEvent(firstEventId)
        dataSource.deleteMember(firstMemberId)
        dataSource.deleteFinanceReport(firstReportId)
        dataSource.deletePaymentObligation(firstPaymentId)

        assertTrue(dataSource.fetchEvents(sectorId).size < initialEvents)
        assertTrue(dataSource.fetchMembers(sectorId).size < initialMembers)
        assertTrue(dataSource.fetchFinanceReports(sectorId).size < initialReports)
        assertTrue(dataSource.fetchPaymentObligations(sectorId).size < initialPayments)

        dataSource.resetSimulationData()

        assertEquals(initialEvents, dataSource.fetchEvents(sectorId).size)
        assertEquals(initialMembers, dataSource.fetchMembers(sectorId).size)
        assertEquals(initialReports, dataSource.fetchFinanceReports(sectorId).size)
        assertEquals(initialPayments, dataSource.fetchPaymentObligations(sectorId).size)
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
