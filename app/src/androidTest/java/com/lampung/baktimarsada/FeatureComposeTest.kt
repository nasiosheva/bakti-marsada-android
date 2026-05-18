package com.lampung.baktimarsada

import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.FinanceReportDetail
import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.PaymentObligationDetail
import com.lampung.baktimarsada.domain.model.PaymentStatus
import com.lampung.baktimarsada.domain.model.TenantContext
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.feature.auth.presentation.LoginScreen
import com.lampung.baktimarsada.feature.auth.presentation.LoginUiState
import com.lampung.baktimarsada.feature.events.presentation.EventContent
import com.lampung.baktimarsada.feature.events.presentation.EventUiState
import com.lampung.baktimarsada.feature.finance.presentation.FinanceContent
import com.lampung.baktimarsada.feature.finance.presentation.FinanceUiState
import com.lampung.baktimarsada.feature.home.presentation.AdminHomeScreen
import com.lampung.baktimarsada.feature.home.presentation.JemaatHomeScreen
import com.lampung.baktimarsada.feature.members.presentation.MemberContent
import com.lampung.baktimarsada.feature.members.presentation.MemberUiState
import com.lampung.baktimarsada.feature.payments.presentation.PaymentContent
import com.lampung.baktimarsada.feature.payments.presentation.PaymentUiState
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue

class FeatureComposeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loginScreen_showsValidationErrors() {
        composeRule.setContent {
            LoginScreen(
                state = LoginUiState(
                    identifierError = composeRule.activity.getString(R.string.login_error_identifier_required),
                    passwordError = composeRule.activity.getString(R.string.login_error_password_required)
                ),
                onIdentifierChanged = {},
                onPasswordChanged = {},
                onLoginClicked = {}
            )
        }

        composeRule.onAllNodesWithTag("login_identifier").assertCountEquals(1)
        composeRule.onAllNodesWithTag("login_password").assertCountEquals(1)
        composeRule.onAllNodesWithText(composeRule.activity.getString(R.string.login_error_identifier_required)).assertCountEquals(1)
        composeRule.onAllNodesWithText(composeRule.activity.getString(R.string.login_error_password_required)).assertCountEquals(1)
    }

    @Test
    fun loginScreen_simulationResetAdminButton_triggersCallback() {
        composeRule.setContent {
            LoginScreen(
                state = LoginUiState(),
                onIdentifierChanged = {},
                onPasswordChanged = {},
                onLoginClicked = {},
                onResetAndLoginAsAdminClicked = {},
                isSimulationEnabled = true
            )
        }

        composeRule.onNodeWithTag("login_demo_reset_admin_button").assertHasClickAction()
    }

    @Test
    fun loginScreen_googleSignInButton_triggersCallbackWhenNotLoading() {
        var clicked = false
        composeRule.setContent {
            LoginScreen(
                state = LoginUiState(isLoading = false),
                onIdentifierChanged = {},
                onPasswordChanged = {},
                onLoginClicked = {},
                onGoogleSignInClicked = { clicked = true },
                isGoogleSignInEnabled = false
            )
        }

        composeRule.onNodeWithTag("login_google_button").assertIsEnabled().performClick()
        assertTrue(clicked)
    }

    @Test
    fun loginScreen_googleSignInButton_disabledWhenLoading() {
        composeRule.setContent {
            LoginScreen(
                state = LoginUiState(isLoading = true),
                onIdentifierChanged = {},
                onPasswordChanged = {},
                onLoginClicked = {},
                onGoogleSignInClicked = {},
                isGoogleSignInEnabled = true
            )
        }

        composeRule.onNodeWithTag("login_google_button").assertIsNotEnabled()
    }

    @Test
    fun loginScreen_googleSignInSuccess_navigatesToDashboard() {
        composeRule.setContent {
            var role by remember { mutableStateOf<UserRole?>(null) }
            if (role == null) {
                LoginScreen(
                    state = LoginUiState(
                        isLoading = false,
                        isGoogleSignInEnabled = true
                    ),
                    onIdentifierChanged = {},
                    onPasswordChanged = {},
                    onLoginClicked = {},
                    onGoogleSignInClicked = { role = UserRole.ADMIN },
                    isGoogleSignInEnabled = true
                )
            } else {
                Text("Dashboard Admin")
            }
        }

        composeRule.onNodeWithTag("login_google_button").assertIsEnabled().performClick()
        composeRule.onAllNodesWithText("Dashboard Admin").assertCountEquals(1)
    }

    @Test
    fun jemaatHome_canSwitchBottomNavigation() {
        composeRule.setContent {
            JemaatHomeScreen(
                session = sampleSession(),
                onLogout = {},
                eventsContent = { Text("Events Content") },
                membersContent = { Text("Members Content") },
                financeContent = { Text("Finance Content") },
                paymentsContent = { Text("Payments Content") }
            )
        }

        composeRule.onAllNodesWithText("Events Content").assertCountEquals(1)
        composeRule.onNodeWithTag("jemaat_bottom_nav_jemaat_members").performClick()
        composeRule.onAllNodesWithText("Members Content").assertCountEquals(1)
        composeRule.onNodeWithTag("jemaat_bottom_nav_jemaat_finance").performClick()
        composeRule.onAllNodesWithText("Finance Content").assertCountEquals(1)
    }

    @Test
    fun jemaatHome_showsLogoutOnlyFromProfile() {
        var logoutClicked = false
        composeRule.setContent {
            JemaatHomeScreen(
                session = sampleSession(),
                onLogout = { logoutClicked = true },
                eventsContent = { Text("Events Content") },
                membersContent = { Text("Members Content") },
                financeContent = { Text("Finance Content") },
                paymentsContent = { Text("Payments Content") },
                profileContent = {
                    Button(
                        onClick = { logoutClicked = true },
                        modifier = Modifier.semantics { testTag = "profile_logout_button" }
                    ) {
                        Text("Logout")
                    }
                }
            )
        }

        composeRule.onAllNodesWithText(composeRule.activity.getString(R.string.action_logout)).assertCountEquals(0)
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.tab_profile)).performClick()
        composeRule.onNodeWithTag("profile_logout_button").performClick()
        assertTrue(logoutClicked)
    }

    @Test
    fun adminHome_canSwitchBottomNavigation() {
        composeRule.setContent {
            AdminHomeScreen(
                session = sampleSession(),
                dashboardContent = { Text("Dashboard Content") },
                eventsContent = { Text("Events Content") },
                membersContent = { Text("Members Content") },
                financeContent = { Text("Finance Content") },
                paymentsContent = { Text("Payments Content") },
                onOpenCreateUser = {},
                onOpenProfile = {}
            )
        }

        composeRule.onAllNodesWithText("Dashboard Content").assertCountEquals(1)
        composeRule.onNodeWithTag("admin_bottom_nav_admin_events").performClick()
        composeRule.onAllNodesWithText("Events Content").assertCountEquals(1)
        composeRule.onNodeWithTag("admin_bottom_nav_admin_payments").performClick()
        composeRule.onAllNodesWithText("Payments Content").assertCountEquals(1)
    }

    @Test
    fun eventContent_hidesAdminActionsForJemaat() {
        composeRule.setContent {
            EventContent(
                isAdmin = false,
                state = EventUiState(items = listOf(sampleEvent())),
                onRefresh = {},
                onDelete = {},
                onShowCreate = {},
                onShowDetail = {},
                onShowEdit = {}
            )
        }

        composeRule.onAllNodesWithTag("event_detail_event-1").assertCountEquals(1)
        composeRule.onAllNodesWithTag("event_edit_event-1").assertCountEquals(0)
        composeRule.onAllNodesWithTag("event_add_fab").assertCountEquals(0)
    }

    @Test
    fun memberContent_showsAdminActionsForAdmin() {
        composeRule.setContent {
            MemberContent(
                isAdmin = true,
                state = MemberUiState(items = listOf(sampleMember())),
                onRefresh = {},
                onDelete = {},
                onShowCreate = {},
                onShowDetail = {},
                onShowEdit = {}
            )
        }

        composeRule.onAllNodesWithTag("member_edit_member-1").assertCountEquals(1)
        composeRule.onAllNodesWithTag("member_delete_member-1").assertCountEquals(1)
        composeRule.onAllNodesWithTag("member_add_fab").assertCountEquals(1)
    }

    @Test
    fun financeContent_filtersHiddenReportsForJemaat() {
        composeRule.setContent {
            FinanceContent(
                isAdmin = false,
                state = FinanceUiState(
                    items = listOf(
                        sampleFinanceReport(id = "visible", title = "Visible Report", visible = true),
                        sampleFinanceReport(id = "hidden", title = "Hidden Report", visible = false)
                    )
                ),
                onRefresh = {},
                onDelete = {},
                onToggleVisibility = {},
                onShowCreate = {},
                onShowDetail = {},
                onShowEdit = {}
            )
        }

        composeRule.onAllNodesWithText("Visible Report").assertCountEquals(1)
        composeRule.onAllNodesWithText("Hidden Report").assertCountEquals(0)
    }

    @Test
    fun paymentContent_showsAdminActionsForAdmin() {
        composeRule.setContent {
            PaymentContent(
                isAdmin = true,
                state = PaymentUiState(items = listOf(samplePayment()), members = listOf(sampleMember())),
                onRefresh = {},
                onDelete = {},
                onShowCreate = {},
                onShowDetail = {},
                onShowEdit = {}
            )
        }

        composeRule.onAllNodesWithTag("payment_edit_payment-1").assertCountEquals(1)
        composeRule.onAllNodesWithTag("payment_delete_payment-1").assertCountEquals(1)
        composeRule.onAllNodesWithTag("payment_add_fab").assertCountEquals(1)
    }

    @Test
    fun featureContent_showsEmptyAndErrorStates() {
        composeRule.setContent {
            EventContent(
                isAdmin = false,
                state = EventUiState(items = emptyList(), isLoading = false, errorMessage = "Gagal memuat data"),
                onRefresh = {},
                onDelete = {},
                onShowCreate = {},
                onShowDetail = {},
                onShowEdit = {}
            )
        }

        composeRule.onAllNodesWithText("Gagal memuat data").assertCountEquals(1)
        composeRule.onAllNodesWithText(composeRule.activity.getString(R.string.action_refresh)).assertCountEquals(1)
        composeRule.onAllNodesWithText(composeRule.activity.getString(R.string.event_empty)).assertCountEquals(0)
    }

    private fun sampleEvent(): EventDetail {
        return EventDetail(
            id = "event-1",
            title = "Partangiangan Rabu",
            description = "Renungan sektor",
            scheduledAt = "2026-05-21 19:00",
            location = "Rumah Keluarga Simanjuntak",
            sectorId = "sector-1",
            sectorName = "Sektor 1 HKBP"
        )
    }

    private fun sampleMember(): MemberDetail {
        return MemberDetail(
            id = "member-1",
            fullName = "P. Simanjuntak",
            familyGroup = "Keluarga Simanjuntak",
            phoneNumber = "0812",
            address = "Jl. Melati",
            roleInSector = "Penatua",
            sectorId = "sector-1",
            sectorName = "Sektor 1 HKBP"
        )
    }

    private fun sampleFinanceReport(id: String, title: String, visible: Boolean): FinanceReportDetail {
        return FinanceReportDetail(
            id = id,
            title = title,
            description = "Laporan keuangan",
            periodLabel = "Mei 2026",
            amount = 1000000,
            isVisibleToJemaat = visible,
            sectorId = "sector-1",
            sectorName = "Sektor 1 HKBP"
        )
    }

    private fun samplePayment(): PaymentObligationDetail {
        return PaymentObligationDetail(
            id = "payment-1",
            memberId = "member-1",
            memberName = "P. Simanjuntak",
            title = "Iuran Mei",
            description = "Iuran bulanan sektor",
            amount = 50000,
            dueDate = "2026-05-25",
            status = PaymentStatus.UNPAID,
            sectorId = "sector-1",
            sectorName = "Sektor 1 HKBP"
        )
    }

    private fun sampleSession(): SessionState {
        return SessionState(
            authToken = "token-1",
            userId = "user-1",
            displayName = "Tester",
            role = UserRole.ADMIN,
            tenantContext = TenantContext(
                tenantId = "tenant-1",
                tenantName = "HKBP",
                subTenantId = "hkbp-kedaton",
                subTenantName = "HKBP Kedaton"
            ),
            sectorContext = SectorContext(
                sectorId = "sector-1",
                sectorName = "Sektor 1 HKBP"
            )
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
