package com.lampung.baktimarsada.feature.home.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.feature.app.navigation.AppRoutes
import com.lampung.baktimarsada.feature.events.presentation.EventRoute
import com.lampung.baktimarsada.feature.finance.presentation.FinanceRoute
import com.lampung.baktimarsada.feature.members.presentation.MemberRoute
import com.lampung.baktimarsada.feature.payments.presentation.PaymentRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JemaatHomeRoute(
    session: SessionState,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppRoutes.JEMAAT_EVENTS
    val tabs = listOf(
        JemaatShellTab(AppRoutes.JEMAAT_EVENTS, stringResource(id = R.string.tab_events)),
        JemaatShellTab(AppRoutes.JEMAAT_MEMBERS, stringResource(id = R.string.tab_members)),
        JemaatShellTab(AppRoutes.JEMAAT_FINANCE, stringResource(id = R.string.tab_finance)),
        JemaatShellTab(AppRoutes.JEMAAT_PAYMENTS, stringResource(id = R.string.tab_payments))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = stringResource(id = R.string.jemaat_home_title))
                        Text(
                            text = session.sectorContext.sectorName,
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
                        modifier = Modifier.semantics { testTag = "jemaat_tab_${tab.route}" },
                        text = { Text(text = tab.label) }
                    )
                }
            }
            NavHost(
                navController = navController,
                startDestination = AppRoutes.JEMAAT_EVENTS,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(AppRoutes.JEMAAT_EVENTS) {
                    EventRoute(isAdmin = false, session = session)
                }
                composable(AppRoutes.JEMAAT_MEMBERS) {
                    MemberRoute(isAdmin = false, session = session)
                }
                composable(AppRoutes.JEMAAT_FINANCE) {
                    FinanceRoute(isAdmin = false, session = session)
                }
                composable(AppRoutes.JEMAAT_PAYMENTS) {
                    PaymentRoute(isAdmin = false, session = session)
                }
            }
        }
    }
}

private data class JemaatShellTab(
    val route: String,
    val label: String
)
