package com.lampung.baktimarsada.feature.home.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppBuildConfig
import com.lampung.baktimarsada.domain.model.SectorContext
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.TenantContext
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.feature.app.navigation.AppRoutes
import com.lampung.baktimarsada.feature.dashboard.presentation.AdminDashboardRoute
import com.lampung.baktimarsada.feature.events.presentation.EventRoute
import com.lampung.baktimarsada.feature.finance.presentation.FinanceRoute
import com.lampung.baktimarsada.feature.members.presentation.MemberRoute
import com.lampung.baktimarsada.feature.payments.presentation.PaymentRoute
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeRoute(
    session: SessionState,
    onOpenCreateUser: () -> Unit,
    onOpenEventDetail: (String) -> Unit,
    onOpenAddEvent: () -> Unit = {},
    onOpenEditEvent: (String) -> Unit = {},
    onOpenProfile: () -> Unit
) {
    AdminHomeScreen(
        session = session,
        dashboardContent = { AdminDashboardRoute(session = session) },
        eventsContent = {
            EventRoute(
                isAdmin = true,
                session = session,
                onOpenDetail = onOpenEventDetail,
                onOpenCreate = onOpenAddEvent,
                onOpenEdit = onOpenEditEvent
            )
        },
        membersContent = { MemberRoute(isAdmin = true, session = session) },
        financeContent = { FinanceRoute(isAdmin = true, session = session) },
        paymentsContent = { PaymentRoute(isAdmin = true, session = session) },
        onOpenCreateUser = onOpenCreateUser,
        onOpenProfile = onOpenProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    session: SessionState,
    dashboardContent: @Composable () -> Unit,
    eventsContent: @Composable () -> Unit,
    membersContent: @Composable () -> Unit,
    financeContent: @Composable () -> Unit,
    paymentsContent: @Composable () -> Unit,
    onOpenCreateUser: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppRoutes.ADMIN_DASHBOARD
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val destinations = listOf(
        AdminBottomDestination(AppRoutes.ADMIN_DASHBOARD, stringResource(id = R.string.tab_dashboard), Icons.Filled.Dashboard),
        AdminBottomDestination(AppRoutes.ADMIN_EVENTS, stringResource(id = R.string.tab_events), Icons.Filled.Event),
        AdminBottomDestination(AppRoutes.ADMIN_MEMBERS, stringResource(id = R.string.tab_members), Icons.Filled.Groups),
        AdminBottomDestination(AppRoutes.ADMIN_FINANCE, stringResource(id = R.string.tab_finance), Icons.Filled.AccountBalanceWallet),
        AdminBottomDestination(AppRoutes.ADMIN_PAYMENTS, stringResource(id = R.string.tab_payments), Icons.Filled.Payments)
    )

    val drawerNavItems = @Composable {
        destinations.forEach { destination ->
            NavigationDrawerItem(
                selected = currentRoute == destination.route,
                onClick = {
                    scope.launch {
                        navController.navigate(destination.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(AppRoutes.ADMIN_DASHBOARD) {
                                saveState = true
                            }
                        }
                        if (isTablet) {
                            drawerState.close()
                    }
                    }
                },
                modifier = Modifier.semantics { testTag = "admin_drawer_nav_${destination.route}" },
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

    val screenContent: @Composable (PaddingValues) -> Unit = { innerPadding ->
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
        }
    }

    val bottomNavItems: @Composable RowScope.() -> Unit = {
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

    val topBar: @Composable () -> Unit = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            navigationIcon = {
                if (isTablet) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = stringResource(id = R.string.app_name)
                        )
                    }
                }
            },
            title = {
                Column {
                    Text(text = stringResource(id = R.string.admin_home_title))
                    Text(
                        text = session.displayName,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (AppBuildConfig.simulationEnabled) {
                        Text(
                            text = stringResource(id = R.string.environment_simulate),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            actions = {
                if (currentRoute == AppRoutes.ADMIN_DASHBOARD) {
                    IconButton(onClick = onOpenCreateUser) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = stringResource(id = R.string.admin_action_create_user)
                        )
                    }
                    IconButton(onClick = onOpenProfile) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = stringResource(id = R.string.tab_profile)
                        )
                    }
                }
            }
        )
    }

    if (isTablet) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Column(modifier = Modifier.fillMaxSize()) {
                        drawerNavItems()
                    }
                }
            }
        ) {
            Scaffold(
                topBar = topBar,
                bottomBar = { }
            ) { innerPadding ->
                screenContent(innerPadding)
            }
        }
    } else {
        Scaffold(
            topBar = topBar,
            bottomBar = {
                NavigationBar {
                    bottomNavItems()
                }
            }
        ) { innerPadding ->
            screenContent(innerPadding)
        }
    }
}

private data class AdminBottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val previewSession = SessionState(
    authToken = "preview",
    userId = "u-1",
    displayName = "Bakti Marsada",
    role = UserRole.ADMIN,
    tenantContext = TenantContext(
        tenantId = "t-1",
        tenantName = "Gereja",
        subTenantId = "s-1",
        subTenantName = "Jakarta"
    ),
    sectorContext = SectorContext(sectorId = "sec-1", sectorName = "Lingkungan 1")
)

@Preview(name = "Home Admin - tabs", widthDp = 412, heightDp = 915)
@Composable
private fun AdminHomeScreenPreview() {
    BaktiMarsadaTheme {
        AdminHomeScreen(
            session = previewSession,
            dashboardContent = { Text("Dashboard placeholder") },
            eventsContent = { Text("Events placeholder") },
            membersContent = { Text("Members placeholder") },
            financeContent = { Text("Finance placeholder") },
            paymentsContent = { Text("Payments placeholder") },
            onOpenCreateUser = {},
            onOpenProfile = {}
        )
    }
}

@Preview(name = "Home Admin - tablet", widthDp = 840, heightDp = 900)
@Composable
private fun AdminHomeTabletPreview() {
    BaktiMarsadaTheme {
        AdminHomeScreen(
            session = previewSession,
            dashboardContent = { Text("Dashboard placeholder") },
            eventsContent = { Text("Events placeholder") },
            membersContent = { Text("Members placeholder") },
            financeContent = { Text("Finance placeholder") },
            paymentsContent = { Text("Payments placeholder") },
            onOpenCreateUser = {},
            onOpenProfile = {}
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
