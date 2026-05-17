package com.lampung.baktimarsada.feature.home.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.feature.app.navigation.AppRoutes
import com.lampung.baktimarsada.feature.dashboard.presentation.AdminDashboardRoute
import com.lampung.baktimarsada.feature.events.presentation.EventRoute
import com.lampung.baktimarsada.feature.finance.presentation.FinanceRoute
import com.lampung.baktimarsada.feature.members.presentation.MemberRoute
import com.lampung.baktimarsada.feature.payments.presentation.PaymentRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeRoute(
    session: SessionState,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppRoutes.ADMIN_DASHBOARD
    val tabs = listOf(
        AdminShellTab(AppRoutes.ADMIN_DASHBOARD, stringResource(id = R.string.tab_dashboard)),
        AdminShellTab(AppRoutes.ADMIN_EVENTS, stringResource(id = R.string.tab_events)),
        AdminShellTab(AppRoutes.ADMIN_MEMBERS, stringResource(id = R.string.tab_members)),
        AdminShellTab(AppRoutes.ADMIN_FINANCE, stringResource(id = R.string.tab_finance)),
        AdminShellTab(AppRoutes.ADMIN_PAYMENTS, stringResource(id = R.string.tab_payments))
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
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text(text = stringResource(id = R.string.action_logout))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(selectedTabIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                launchSingleTop = true
                            }
                        },
                        text = { Text(text = tab.label) }
                    )
                }
            }
            NavHost(
                navController = navController,
                startDestination = AppRoutes.ADMIN_DASHBOARD,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(AppRoutes.ADMIN_DASHBOARD) {
                    AdminDashboardRoute(session = session)
                }
                composable(AppRoutes.ADMIN_EVENTS) {
                    EventRoute(isAdmin = true, session = session)
                }
                composable(AppRoutes.ADMIN_MEMBERS) {
                    MemberRoute(isAdmin = true, session = session)
                }
                composable(AppRoutes.ADMIN_FINANCE) {
                    FinanceRoute(isAdmin = true, session = session)
                }
                composable(AppRoutes.ADMIN_PAYMENTS) {
                    PaymentRoute(isAdmin = true, session = session)
                }
            }
        }
    }
}

private data class AdminShellTab(
    val route: String,
    val label: String
)
