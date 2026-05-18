package com.lampung.baktimarsada.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.lampung.baktimarsada.feature.events.presentation.EventRoute
import com.lampung.baktimarsada.feature.finance.presentation.FinanceRoute
import com.lampung.baktimarsada.feature.members.presentation.MemberRoute
import com.lampung.baktimarsada.feature.payments.presentation.PaymentRoute
import com.lampung.baktimarsada.feature.profile.presentation.ProfileRoute
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JemaatHomeRoute(
    session: SessionState,
    onLogout: () -> Unit,
    onOpenEventDetail: (String) -> Unit
) {
    JemaatHomeScreen(
        session = session,
        onLogout = onLogout,
        eventsContent = { EventRoute(isAdmin = false, session = session, onOpenDetail = onOpenEventDetail) },
        membersContent = { MemberRoute(isAdmin = false, session = session) },
        financeContent = { FinanceRoute(isAdmin = false, session = session) },
        paymentsContent = { PaymentRoute(isAdmin = false, session = session) },
        profileContent = { ProfileRoute(session = session, onLogout = onLogout) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JemaatHomeScreen(
    session: SessionState,
    onLogout: () -> Unit,
    eventsContent: @Composable () -> Unit,
    membersContent: @Composable () -> Unit,
    financeContent: @Composable () -> Unit,
    paymentsContent: @Composable () -> Unit,
    profileContent: @Composable () -> Unit = { ProfileRoute(session = session, onLogout = onLogout) }
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppRoutes.JEMAAT_EVENTS
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val destinations = listOf(
        JemaatBottomDestination(AppRoutes.JEMAAT_EVENTS, stringResource(id = R.string.tab_events), Icons.Filled.Event),
        JemaatBottomDestination(AppRoutes.JEMAAT_MEMBERS, stringResource(id = R.string.tab_members), Icons.Filled.Groups),
        JemaatBottomDestination(AppRoutes.JEMAAT_FINANCE, stringResource(id = R.string.tab_finance), Icons.Filled.AccountBalanceWallet),
        JemaatBottomDestination(AppRoutes.JEMAAT_PAYMENTS, stringResource(id = R.string.tab_payments), Icons.Filled.Payments)
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
                            popUpTo(AppRoutes.JEMAAT_EVENTS) {
                                saveState = true
                            }
                        }
                        if (isTablet) {
                            drawerState.close()
                        }
                    }
                },
                modifier = Modifier.semantics { testTag = "jemaat_drawer_nav_${destination.route}" },
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

    val bottomNavItems: @Composable RowScope.() -> Unit = {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = {
                    navController.navigate(destination.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(AppRoutes.JEMAAT_EVENTS) {
                            saveState = true
                        }
                    }
                },
                modifier = Modifier.semantics { testTag = "jemaat_bottom_nav_${destination.route}" },
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

    val content: @Composable (PaddingValues) -> Unit = { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.20f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            NavHost(
                navController = navController,
                startDestination = AppRoutes.JEMAAT_EVENTS,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(AppRoutes.JEMAAT_EVENTS) {
                    eventsContent()
                }
                composable(AppRoutes.JEMAAT_MEMBERS) {
                    membersContent()
                }
                composable(AppRoutes.JEMAAT_FINANCE) {
                    financeContent()
                }
                composable(AppRoutes.JEMAAT_PAYMENTS) {
                    paymentsContent()
                }
                composable(AppRoutes.JEMAAT_PROFILE) {
                    profileContent()
                }
            }
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
                    Text(text = stringResource(id = R.string.jemaat_home_title))
                    Text(
                        text = session.sectorContext.sectorName,
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
                IconButton(
                    onClick = {
                        navController.navigate(AppRoutes.JEMAAT_PROFILE) {
                            launchSingleTop = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = stringResource(id = R.string.tab_profile)
                    )
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
                content(innerPadding)
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
            content(innerPadding)
        }
    }
}

private data class JemaatBottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Preview(name = "Jemaat Home", showBackground = true)
@Composable
private fun JemaatHomeScreenPreview() {
    val previewSession = SessionState(
        authToken = "preview-token",
        userId = "jemaat-1",
        displayName = "Jemaat Kedaton",
        role = UserRole.JEMAAT,
        tenantContext = TenantContext(
            tenantId = "hkbp",
            tenantName = "HKBP",
            subTenantId = "hkbp-kedaton",
            subTenantName = "HKBP Kedaton"
        ),
        sectorContext = SectorContext(
            sectorId = "sector-1",
            sectorName = "Sektor 1 HKBP Kedaton"
        )
    )

    BaktiMarsadaTheme {
        JemaatHomeScreen(
            session = previewSession,
            onLogout = {},
            eventsContent = { Text("Events placeholder") },
            membersContent = { Text("Members placeholder") },
            financeContent = { Text("Finance placeholder") },
            paymentsContent = { Text("Payments placeholder") },
            profileContent = { Text("Profile placeholder") }
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
