package com.lampung.baktimarsada.feature.dashboard.presentation

import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.resources.StringProvider
import com.lampung.baktimarsada.data.remote.AppRemoteDataSource
import com.lampung.baktimarsada.network.dto.ArisanParticipantDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountRequestDto
import com.lampung.baktimarsada.network.dto.CreateUserAccountResponseDto
import com.lampung.baktimarsada.network.dto.EventDto
import com.lampung.baktimarsada.network.dto.FinanceReportDto
import com.lampung.baktimarsada.network.dto.GoogleLoginRequestDto
import com.lampung.baktimarsada.network.dto.LoginRequestDto
import com.lampung.baktimarsada.network.dto.MemberDto
import com.lampung.baktimarsada.network.dto.PaymentObligationDto
import com.lampung.baktimarsada.network.dto.SessionResponseDto
import com.lampung.baktimarsada.network.dto.WorshipTemplateDto
import com.lampung.baktimarsada.test.MainDispatcherRule
import com.lampung.baktimarsada.test.fakes.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminUserCreateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `createUser sets success message and refreshes users`() = runTest {
        val remote = FakeRemoteDataSource()
        val viewModel = AdminUserCreateViewModel(
            remoteDataSource = remote,
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher),
            stringProvider = FakeStringProvider()
        )
        advanceUntilIdle()

        viewModel.createUser(
            CreateUserAccountRequestDto(
                username = "admin.new",
                email = "admin.new@example.com",
                role = "ADMIN",
                password = "Password123",
                fullName = "Admin Baru"
            )
        )
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.successMessage)
        assertEquals(2, viewModel.state.value.adminUsers.size)
        assertEquals("admin.new@example.com", viewModel.state.value.adminUsers.last().email)
    }

    private class FakeStringProvider : StringProvider {
        override fun get(resId: Int): String {
            return when (resId) {
                R.string.admin_create_user_success -> "User berhasil dibuat"
                R.string.admin_create_user_error_default -> "Terjadi kesalahan"
                else -> "string-$resId"
            }
        }

        override fun get(resId: Int, vararg args: Any): String = get(resId)
    }

    private class FakeRemoteDataSource : AppRemoteDataSource {
        private val users = mutableListOf(
            CreateUserAccountResponseDto(
                id = "admin-1",
                username = "admin1",
                email = "admin1@example.com",
                role = "ADMIN",
                fullName = "Admin 1"
            )
        )

        override suspend fun createUserAccount(request: CreateUserAccountRequestDto): CreateUserAccountResponseDto {
            val created = CreateUserAccountResponseDto(
                id = "user-${users.size + 1}",
                username = request.username,
                email = request.email,
                role = request.role,
                fullName = request.fullName
            )
            users.add(created)
            return created
        }

        override suspend fun fetchUsersByRole(role: String): List<CreateUserAccountResponseDto> {
            return users.filter { it.role.equals(role, ignoreCase = true) }
        }

        override suspend fun login(request: LoginRequestDto): SessionResponseDto = unsupported()
        override suspend fun loginWithGoogle(request: GoogleLoginRequestDto): SessionResponseDto = unsupported()
        override suspend fun registerNewTenant(request: com.lampung.baktimarsada.network.dto.RegisterTenantRequestDto): SessionResponseDto = unsupported()
        override suspend fun logout(token: String) = Unit
        override suspend fun syncFcmToken(token: String) = Unit
        override suspend fun fetchEvents(tenantId: String, sectorId: String): List<EventDto> = unsupported()
        override suspend fun saveEvent(tenantId: String, event: EventDto): EventDto = unsupported()
        override suspend fun deleteEvent(tenantId: String, eventId: String) = Unit
        override suspend fun fetchMembers(tenantId: String, sectorId: String): List<MemberDto> = unsupported()
        override suspend fun saveMember(tenantId: String, member: MemberDto): MemberDto = unsupported()
        override suspend fun deleteMember(tenantId: String, memberId: String) = Unit
        override suspend fun fetchFinanceReports(tenantId: String, sectorId: String): List<FinanceReportDto> = unsupported()
        override suspend fun saveFinanceReport(tenantId: String, report: FinanceReportDto): FinanceReportDto = unsupported()
        override suspend fun deleteFinanceReport(tenantId: String, reportId: String) = Unit
        override suspend fun fetchPaymentObligations(tenantId: String, sectorId: String): List<PaymentObligationDto> = unsupported()
        override suspend fun savePaymentObligation(tenantId: String, obligation: PaymentObligationDto): PaymentObligationDto = unsupported()
        override suspend fun deletePaymentObligation(tenantId: String, obligationId: String) = Unit
        override suspend fun fetchArisanParticipants(tenantId: String, sectorId: String): List<ArisanParticipantDto> = emptyList()
        override suspend fun replaceArisanParticipants(tenantId: String, sectorId: String, memberIds: List<String>): List<ArisanParticipantDto> = emptyList()
        override suspend fun fillArisanParticipantsFromMembers(tenantId: String, sectorId: String): List<ArisanParticipantDto> = emptyList()
        override suspend fun fetchWorshipTemplates(tenantId: String, sectorId: String): List<WorshipTemplateDto> = emptyList()
        override suspend fun saveWorshipTemplate(tenantId: String, template: WorshipTemplateDto): WorshipTemplateDto = template
        override suspend fun deleteWorshipTemplate(tenantId: String, templateId: String) = Unit
        override suspend fun fetchTenantProfile(tenantId: String): com.lampung.baktimarsada.network.dto.TenantProfileDto = unsupported()
        override suspend fun resetSimulationData() = Unit

        private fun unsupported(): Nothing = error("unsupported")
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
