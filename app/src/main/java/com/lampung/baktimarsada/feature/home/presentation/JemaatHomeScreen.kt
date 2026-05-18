package com.lampung.baktimarsada.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import com.lampung.baktimarsada.ui.component.BaktiBottomNavItem
import com.lampung.baktimarsada.ui.component.BaktiBottomNavigationBar
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JemaatHomeRoute(
    session: SessionState,
    onLogout: () -> Unit,
    onOpenEventDetail: (String) -> Unit,
    onOpenProfile: () -> Unit
) {
    JemaatHomeScreen(
        session = session,
        onLogout = onLogout,
        eventsContent = { bottomContentPadding ->
            EventRoute(
                isAdmin = false,
                session = session,
                onOpenDetail = onOpenEventDetail,
                bottomContentPadding = bottomContentPadding
            )
        },
        membersContent = { MemberRoute(isAdmin = false, session = session) },
        financeContent = { FinanceRoute(isAdmin = false, session = session) },
        paymentsContent = { PaymentRoute(isAdmin = false, session = session) },
        onOpenProfile = onOpenProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JemaatHomeScreen(
    session: SessionState,
    onLogout: () -> Unit,
    eventsContent: @Composable (bottomContentPadding: Dp) -> Unit,
    membersContent: @Composable () -> Unit,
    financeContent: @Composable () -> Unit,
    paymentsContent: @Composable () -> Unit,
    onOpenProfile: () -> Unit
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

    val bottomNavItems = destinations.map { destination ->
        BaktiBottomNavItem(
            route = destination.route,
            label = destination.label,
            icon = destination.icon,
            testTag = "jemaat_bottom_nav_${destination.route}"
        )
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
                    eventsContent(innerPadding.calculateBottomPadding())
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
            }
        }
    }

    val topBar: @Composable () -> Unit = {
        JemaatHomeTopBar(
            session = session,
            currentSection = destinations.currentLabel(currentRoute)
                .ifBlank { stringResource(id = R.string.tab_profile) },
            isTablet = isTablet,
            onOpenDrawer = { scope.launch { drawerState.open() } },
            onOpenProfile = onOpenProfile
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
                BaktiBottomNavigationBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onRouteSelected = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(AppRoutes.JEMAAT_EVENTS) {
                                saveState = true
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.navigationBarsPadding(),
                    itemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JemaatHomeTopBar(
    session: SessionState,
    currentSection: String,
    isTablet: Boolean,
    onOpenDrawer: () -> Unit,
    onOpenProfile: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        navigationIcon = {
            if (isTablet) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(id = R.string.app_name)
                    )
                }
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    tonalElevation = 3.dp
                ) {
                    Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(
                            text = session.displayName.initials(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.jemaat_home_greeting),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (AppBuildConfig.simulationEnabled) {
                            JemaatInfoPill(text = stringResource(id = R.string.environment_simulate))
                        }
                    }
                    Text(
                        text = session.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(
                            id = R.string.jemaat_home_sector_format,
                            session.sectorContext.sectorName
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                JemaatInfoPill(text = currentSection)
            }
        },
        actions = {
            IconButton(onClick = onOpenProfile) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = stringResource(id = R.string.tab_profile)
                )
            }
        }
    )
}

@Composable
private fun JemaatInfoPill(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private data class JemaatBottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private fun List<JemaatBottomDestination>.currentLabel(route: String): String {
    return firstOrNull { it.route == route }?.label.orEmpty()
}

private fun String.initials(): String {
    return trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString(separator = "") { it.first().uppercase() }
        .ifBlank { "J" }
}

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
            eventsContent = { _ -> Text("Events placeholder") },
            membersContent = { Text("Members placeholder") },
            financeContent = { Text("Finance placeholder") },
            paymentsContent = { Text("Payments placeholder") },
            onOpenProfile = {}
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
