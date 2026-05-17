package com.lampung.baktimarsada.feature.home.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.feature.app.navigation.AppRoutes
import com.lampung.baktimarsada.feature.dashboard.presentation.AdminDashboardRoute
import com.lampung.baktimarsada.feature.events.presentation.EventRoute
import com.lampung.baktimarsada.feature.finance.presentation.FinanceRoute
import com.lampung.baktimarsada.feature.members.presentation.MemberRoute
import com.lampung.baktimarsada.feature.payments.presentation.PaymentRoute
import com.lampung.baktimarsada.feature.profile.presentation.ProfileRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeRoute(
    session: SessionState,
    onLogout: () -> Unit,
    onOpenEventDetail: (String) -> Unit
) {
    AdminHomeScreen(
        session = session,
        onLogout = onLogout,
        dashboardContent = { AdminDashboardRoute(session = session) },
        eventsContent = { EventRoute(isAdmin = true, session = session, onOpenDetail = onOpenEventDetail) },
        membersContent = { MemberRoute(isAdmin = true, session = session) },
        financeContent = { FinanceRoute(isAdmin = true, session = session) },
        paymentsContent = { PaymentRoute(isAdmin = true, session = session) },
        profileContent = { ProfileRoute(session = session, onLogout = onLogout) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    session: SessionState,
    onLogout: () -> Unit,
    dashboardContent: @Composable () -> Unit,
    eventsContent: @Composable () -> Unit,
    membersContent: @Composable () -> Unit,
    financeContent: @Composable () -> Unit,
    paymentsContent: @Composable () -> Unit,
    profileContent: @Composable () -> Unit = { ProfileRoute(session = session, onLogout = onLogout) }
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppRoutes.ADMIN_DASHBOARD
    val destinations = listOf(
        AdminBottomDestination(AppRoutes.ADMIN_DASHBOARD, stringResource(id = R.string.tab_dashboard), Icons.Filled.Dashboard),
        AdminBottomDestination(AppRoutes.ADMIN_EVENTS, stringResource(id = R.string.tab_events), Icons.Filled.Event),
        AdminBottomDestination(AppRoutes.ADMIN_MEMBERS, stringResource(id = R.string.tab_members), Icons.Filled.Groups),
        AdminBottomDestination(AppRoutes.ADMIN_FINANCE, stringResource(id = R.string.tab_finance), Icons.Filled.AccountBalanceWallet),
        AdminBottomDestination(AppRoutes.ADMIN_PAYMENTS, stringResource(id = R.string.tab_payments), Icons.Filled.Payments),
        AdminBottomDestination(AppRoutes.ADMIN_PROFILE, stringResource(id = R.string.tab_profile), Icons.Filled.Person)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = stringResource(id = R.string.admin_home_title))
                        Text(
                            text = session.displayName,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (AppConstants.SIMULATION_ENABLED) {
                            Text(
                                text = stringResource(id = R.string.environment_simulate),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(AppRoutes.ADMIN_DASHBOARD) {
                                    saveState = true
                                }
                            }
                        },
                        modifier = Modifier.semantics { testTag = "admin_bottom_nav_${destination.route}" },
                        label = { Text(text = destination.label) },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.ADMIN_DASHBOARD,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(AppRoutes.ADMIN_DASHBOARD) {
                dashboardContent()
            }
            composable(AppRoutes.ADMIN_EVENTS) {
                eventsContent()
            }
            composable(AppRoutes.ADMIN_MEMBERS) {
                membersContent()
            }
            composable(AppRoutes.ADMIN_FINANCE) {
                financeContent()
            }
            composable(AppRoutes.ADMIN_PAYMENTS) {
                paymentsContent()
            }
            composable(AppRoutes.ADMIN_PROFILE) {
                profileContent()
            }
        }
    }
}

private data class AdminBottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)
