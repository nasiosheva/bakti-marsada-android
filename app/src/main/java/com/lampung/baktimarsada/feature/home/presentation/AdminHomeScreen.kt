package com.lampung.baktimarsada.feature.home.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.lampung.baktimarsada.feature.arisan.presentation.ArisanRoute
import com.lampung.baktimarsada.feature.dashboard.presentation.AdminDashboardRoute
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
        eventsContent = { bottomContentPadding ->
            EventRoute(
                isAdmin = true,
                session = session,
                onOpenDetail = onOpenEventDetail,
                onOpenCreate = onOpenAddEvent,
                onOpenEdit = onOpenEditEvent,
                bottomContentPadding = bottomContentPadding
            )
        },
        membersContent = { MemberRoute(isAdmin = true, session = session) },
        financeContent = { FinanceRoute(isAdmin = true, session = session) },
        paymentsContent = { PaymentRoute(isAdmin = true, session = session) },
        arisanContent = { bottomContentPadding ->
            ArisanRoute(
                isAdmin = true,
                session = session,
                bottomContentPadding = bottomContentPadding
            )
        },
        onOpenCreateUser = onOpenCreateUser,
        onOpenProfile = onOpenProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    session: SessionState,
    dashboardContent: @Composable () -> Unit,
    eventsContent: @Composable (bottomContentPadding: Dp) -> Unit,
    membersContent: @Composable () -> Unit,
    financeContent: @Composable () -> Unit,
    paymentsContent: @Composable () -> Unit,
    arisanContent: @Composable (bottomContentPadding: Dp) -> Unit,
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
        AdminBottomDestination(AppRoutes.ADMIN_PAYMENTS, stringResource(id = R.string.tab_payments), Icons.Filled.Payments),
        AdminBottomDestination(AppRoutes.ADMIN_ARISAN, stringResource(id = R.string.tab_arisan), Icons.Filled.Savings)
    )
    val currentSection = destinations.currentLabel(currentRoute)
        .ifBlank { stringResource(id = R.string.admin_home_title) }

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
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .semantics { testTag = "admin_drawer_nav_${destination.route}" },
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
            testTag = "admin_bottom_nav_${destination.route}"
        )
    }

    val screenContent: @Composable (PaddingValues) -> Unit = { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AdminSectionBanner(
                    sectorName = session.sectorContext.sectorName,
                    currentSection = currentSection
                )
                NavHost(
                    navController = navController,
                    startDestination = AppRoutes.ADMIN_DASHBOARD,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(AppRoutes.ADMIN_DASHBOARD) {
                        dashboardContent()
                    }
                    composable(AppRoutes.ADMIN_EVENTS) {
                        eventsContent(innerPadding.calculateBottomPadding())
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
                    composable(AppRoutes.ADMIN_ARISAN) {
                        arisanContent(innerPadding.calculateBottomPadding())
                    }
                }
            }
        }
    }

    val topBar: @Composable () -> Unit = {
        AdminHomeTopBar(
            session = session,
            isTablet = isTablet,
            isDashboard = currentRoute == AppRoutes.ADMIN_DASHBOARD,
            onOpenDrawer = { scope.launch { drawerState.open() } },
            onOpenCreateUser = onOpenCreateUser,
            onOpenProfile = onOpenProfile
        )
    }

    if (isTablet) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        drawerNavItems()
                    }
                }
            }
        ) {
            Scaffold(
                topBar = topBar,
                bottomBar = { },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                screenContent(innerPadding)
            }
        }
    } else {
        Scaffold(
            topBar = topBar,
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                BaktiBottomNavigationBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onRouteSelected = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(AppRoutes.ADMIN_DASHBOARD) {
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
            screenContent(innerPadding)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminHomeTopBar(
    session: SessionState,
    isTablet: Boolean,
    isDashboard: Boolean,
    onOpenDrawer: () -> Unit,
    onOpenCreateUser: () -> Unit,
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenProfile),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer),
                    tonalElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = session.displayName.adminInitials(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.admin_home_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (AppBuildConfig.simulationEnabled) {
                            AdminInfoPill(
                                text = stringResource(id = R.string.environment_simulate),
                                tonal = true
                            )
                        }
                    }
                    Text(
                        text = session.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            if (isDashboard) {
                IconButton(onClick = onOpenCreateUser) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = stringResource(id = R.string.admin_action_create_user)
                    )
                }
            }
        }
    )
}

@Composable
private fun AdminSectionBanner(
    sectorName: String,
    currentSection: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = sectorName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentSection,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminInfoPill(text: String, tonal: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (tonal) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        },
        contentColor = if (tonal) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        },
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private data class AdminBottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private fun List<AdminBottomDestination>.currentLabel(route: String): String {
    return firstOrNull { it.route == route }?.label.orEmpty()
}

private fun String.adminInitials(): String {
    return trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString(separator = "") { it.first().uppercase() }
        .ifBlank { "A" }
}

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
    sectorContext = SectorContext(sectorId = "sec-1", sectorName = "Lingkungan 1 HKBP Kedaton")
)

@Preview(name = "Admin Home - Phone", widthDp = 412, heightDp = 915)
@Composable
private fun AdminHomeScreenPreview() {
    BaktiMarsadaTheme {
        AdminHomeScreen(
            session = previewSession,
            dashboardContent = { Text("Dashboard placeholder", modifier = Modifier.padding(16.dp)) },
            eventsContent = { _ -> Text("Events placeholder", modifier = Modifier.padding(16.dp)) },
            membersContent = { Text("Members placeholder", modifier = Modifier.padding(16.dp)) },
            financeContent = { Text("Finance placeholder", modifier = Modifier.padding(16.dp)) },
            paymentsContent = { Text("Payments placeholder", modifier = Modifier.padding(16.dp)) },
            arisanContent = { _ -> Text("Arisan placeholder", modifier = Modifier.padding(16.dp)) },
            onOpenCreateUser = {},
            onOpenProfile = {}
        )
    }
}

@Preview(
    name = "Admin Home - Phone Dark",
    widthDp = 412,
    heightDp = 915,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AdminHomeScreenDarkPreview() {
    BaktiMarsadaTheme {
        AdminHomeScreen(
            session = previewSession,
            dashboardContent = { Text("Dashboard placeholder", modifier = Modifier.padding(16.dp)) },
            eventsContent = { _ -> Text("Events placeholder", modifier = Modifier.padding(16.dp)) },
            membersContent = { Text("Members placeholder", modifier = Modifier.padding(16.dp)) },
            financeContent = { Text("Finance placeholder", modifier = Modifier.padding(16.dp)) },
            paymentsContent = { Text("Payments placeholder", modifier = Modifier.padding(16.dp)) },
            arisanContent = { _ -> Text("Arisan placeholder", modifier = Modifier.padding(16.dp)) },
            onOpenCreateUser = {},
            onOpenProfile = {}
        )
    }
}

@Preview(name = "Admin Home - Tablet", widthDp = 840, heightDp = 900)
@Composable
private fun AdminHomeTabletPreview() {
    BaktiMarsadaTheme {
        AdminHomeScreen(
            session = previewSession,
            dashboardContent = { Text("Dashboard placeholder", modifier = Modifier.padding(16.dp)) },
            eventsContent = { _ -> Text("Events placeholder", modifier = Modifier.padding(16.dp)) },
            membersContent = { Text("Members placeholder", modifier = Modifier.padding(16.dp)) },
            financeContent = { Text("Finance placeholder", modifier = Modifier.padding(16.dp)) },
            paymentsContent = { Text("Payments placeholder", modifier = Modifier.padding(16.dp)) },
            arisanContent = { _ -> Text("Arisan placeholder", modifier = Modifier.padding(16.dp)) },
            onOpenCreateUser = {},
            onOpenProfile = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "AdminHomeTopBar - Dashboard", showBackground = true)
@Composable
private fun AdminHomeTopBarDashboardPreview() {
    BaktiMarsadaTheme {
        AdminHomeTopBar(
            session = previewSession,
            isTablet = false,
            isDashboard = true,
            onOpenDrawer = {},
            onOpenCreateUser = {},
            onOpenProfile = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "AdminHomeTopBar - Other Tab", showBackground = true)
@Composable
private fun AdminHomeTopBarOtherPreview() {
    BaktiMarsadaTheme {
        AdminHomeTopBar(
            session = previewSession,
            isTablet = false,
            isDashboard = false,
            onOpenDrawer = {},
            onOpenCreateUser = {},
            onOpenProfile = {}
        )
    }
}

@Preview(name = "AdminSectionBanner", showBackground = true, widthDp = 412)
@Composable
private fun AdminSectionBannerPreview() {
    BaktiMarsadaTheme {
        AdminSectionBanner(
            sectorName = "Sektor 1 HKBP Kedaton",
            currentSection = "Dashboard"
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
