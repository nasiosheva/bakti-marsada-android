package com.lampung.baktimarsada.feature.app.presentation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.repository.EventRepository
import com.lampung.baktimarsada.repository.FinanceReportRepository
import com.lampung.baktimarsada.repository.MemberRepository
import com.lampung.baktimarsada.repository.PaymentObligationRepository
import com.lampung.baktimarsada.domain.usecase.BootstrapSessionUseCase
import com.lampung.baktimarsada.domain.usecase.LogoutUseCase
import com.lampung.baktimarsada.domain.usecase.ObserveSessionUseCase
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.feature.app.navigation.AppRoutes
import com.lampung.baktimarsada.feature.auth.presentation.LoginRoute
import com.lampung.baktimarsada.feature.events.presentation.EventCreateRoute
import com.lampung.baktimarsada.feature.events.presentation.EventCopySourceRoute
import com.lampung.baktimarsada.feature.events.presentation.EventEditRoute
import com.lampung.baktimarsada.feature.events.presentation.EventDetailRoute
import com.lampung.baktimarsada.feature.events.presentation.EventTemplateCreateRoute
import com.lampung.baktimarsada.feature.events.presentation.EventTemplateEditRoute
import com.lampung.baktimarsada.feature.events.presentation.EventTemplateRoute
import com.lampung.baktimarsada.feature.home.presentation.AdminHomeRoute
import com.lampung.baktimarsada.feature.home.presentation.JemaatHomeRoute
import com.lampung.baktimarsada.feature.profile.presentation.ProfileRoute
import com.lampung.baktimarsada.feature.dashboard.presentation.AdminUserCreateRoute
import com.lampung.baktimarsada.firebase.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun BaktiMarsadaApp(
    viewModel: AppEntryViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context as? Activity }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val lastBackPressedAt = remember { mutableLongStateOf(0L) }
    val isAtRootHome = currentBackStackEntry?.destination?.route in setOf(
        AppRoutes.JEMAAT_HOME,
        AppRoutes.ADMIN_HOME
    )

    LaunchedEffect(state.pendingRoute) {
        state.pendingRoute?.let { route ->
            navController.navigateAndClear(route)
            viewModel.consumePendingRoute()
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.SPLASH
    ) {
        composable(AppRoutes.SPLASH) {
            SplashRoute()
        }
        composable(AppRoutes.LOGIN) {
            LoginRoute(
                onLoginSuccess = { role ->
                    navController.navigateAndClear(role.toHomeRoute())
                }
            )
        }
        composable(AppRoutes.JEMAAT_HOME) {
            state.session?.let { session ->
                JemaatHomeRoute(
                    session = session,
                    onLogout = { viewModel.logout() },
                    onOpenEventDetail = { eventId ->
                        navController.navigate(AppRoutes.jemaatEventDetail(eventId))
                    },
                    onOpenProfile = {
                        navController.navigate(AppRoutes.JEMAAT_PROFILE) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        composable(AppRoutes.JEMAAT_PROFILE) {
            state.session?.let { session ->
                ProfileRoute(
                    session = session,
                    onBack = { navController.navigateUp() },
                    onTestSendNotification = { viewModel.sendTestNotification() },
                    onLogout = { viewModel.logout() }
                )
            }
        }
        composable(AppRoutes.ADMIN_HOME) {
            state.session?.let { session ->
                AdminHomeRoute(
                    session = session,
                    onOpenCreateUser = {
                        navController.navigate(AppRoutes.ADMIN_USER_CREATE)
                    },
                    onOpenEventDetail = { eventId ->
                        navController.navigate(AppRoutes.adminEventDetail(eventId))
                    },
                    onOpenAddEvent = {
                        navController.navigate(AppRoutes.ADMIN_EVENT_CREATE)
                    },
                    onOpenEditEvent = { eventId ->
                        navController.navigate(AppRoutes.adminEventEdit(eventId))
                    },
                    onOpenProfile = {
                        navController.navigate(AppRoutes.ADMIN_PROFILE) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        composable(AppRoutes.ADMIN_PROFILE) {
            state.session?.let { session ->
                ProfileRoute(
                    session = session,
                    onBack = { navController.navigateUp() },
                    onTestSendNotification = { viewModel.sendTestNotification() },
                    onLogout = { viewModel.logout() }
                )
            }
        }
        composable(AppRoutes.ADMIN_USER_CREATE) {
            AdminUserCreateRoute(
                onBack = { navController.navigateUp() }
            )
        }
        composable(AppRoutes.JEMAAT_EVENT_DETAIL) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString(AppRoutes.EVENT_ID_ARG).orEmpty()
            state.session?.let { session ->
                EventDetailRoute(
                    eventId = eventId,
                    isAdmin = false,
                    session = session,
                    onBack = { navController.navigateUp() }
                )
            }
        }
        composable(AppRoutes.ADMIN_EVENT_DETAIL) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString(AppRoutes.EVENT_ID_ARG).orEmpty()
            state.session?.let { session ->
                EventDetailRoute(
                    eventId = eventId,
                    isAdmin = true,
                    session = session,
                    onBack = { navController.navigateUp() }
                )
            }
        }
        composable(AppRoutes.ADMIN_EVENT_CREATE) {
            state.session?.let { session ->
                EventCreateRoute(
                    session = session,
                    copyFromEventId = null,
                    onOpenTemplates = { navController.navigate(AppRoutes.ADMIN_EVENT_TEMPLATES) },
                    onOpenCopyFromPrevious = {
                        navController.navigate(AppRoutes.ADMIN_EVENT_COPY_SOURCE)
                    },
                    onBack = { navController.navigateUp() }
                )
            }
        }
        composable(AppRoutes.ADMIN_EVENT_CREATE_FROM) { backStackEntry ->
            val sourceEventId = backStackEntry.arguments?.getString(AppRoutes.SOURCE_EVENT_ID_ARG)
            state.session?.let { session ->
                EventCreateRoute(
                    session = session,
                    copyFromEventId = sourceEventId,
                    onOpenTemplates = { navController.navigate(AppRoutes.ADMIN_EVENT_TEMPLATES) },
                    onOpenCopyFromPrevious = {
                        navController.navigate(AppRoutes.ADMIN_EVENT_COPY_SOURCE)
                    },
                    onBack = { navController.navigateUp() }
                )
            }
        }
        composable(AppRoutes.ADMIN_EVENT_COPY_SOURCE) {
            EventCopySourceRoute(
                onBack = { navController.navigateUp() },
                onSelectEvent = { eventId ->
                    navController.navigate(AppRoutes.adminEventCreateFrom(eventId)) {
                        popUpTo(AppRoutes.ADMIN_EVENT_CREATE) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(AppRoutes.ADMIN_EVENT_EDIT) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString(AppRoutes.EVENT_ID_ARG).orEmpty()
            state.session?.let { session ->
                EventEditRoute(
                    eventId = eventId,
                    session = session,
                    onBack = { navController.navigateUp() }
                )
            }
        }
        composable(AppRoutes.ADMIN_EVENT_TEMPLATES) {
            state.session?.let { session ->
                EventTemplateRoute(
                    session = session,
                    onBack = { navController.navigateUp() },
                    onOpenCreate = { navController.navigate(AppRoutes.ADMIN_EVENT_TEMPLATE_CREATE) },
                    onOpenEdit = { templateId ->
                        navController.navigate(AppRoutes.adminEventTemplateEdit(templateId))
                    }
                )
            }
        }
        composable(AppRoutes.ADMIN_EVENT_TEMPLATE_CREATE) {
            state.session?.let { session ->
                EventTemplateCreateRoute(
                    session = session,
                    onBack = { navController.navigateUp() }
                )
            }
        }
        composable(AppRoutes.ADMIN_EVENT_TEMPLATE_EDIT) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString(AppRoutes.TEMPLATE_ID_ARG).orEmpty()
            state.session?.let { session ->
                EventTemplateEditRoute(
                    templateId = templateId,
                    session = session,
                    onBack = { navController.navigateUp() }
                )
            }
        }
    }

    BackHandler(enabled = isAtRootHome) {
        val now = System.currentTimeMillis()
        val lastPress = lastBackPressedAt.longValue
        if (now - lastPress < 1500) {
            activity?.finish()
            return@BackHandler
        }
        lastBackPressedAt.longValue = now
        Toast.makeText(context, R.string.exit_app_confirmation, Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun SplashRoute() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(id = R.string.splash_message),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun NavHostController.navigateAndClear(route: String) {
    navigate(route) {
        popUpTo(graph.id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

private fun UserRole.toHomeRoute(): String {
    return when (this) {
        UserRole.JEMAAT -> AppRoutes.JEMAAT_HOME
        UserRole.ADMIN -> AppRoutes.ADMIN_HOME
    }
}

data class AppEntryUiState(
    val session: SessionState? = null,
    val pendingRoute: String? = null
)

@HiltViewModel
class AppEntryViewModel @Inject constructor(
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val bootstrapSessionUseCase: BootstrapSessionUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val eventRepository: EventRepository,
    private val memberRepository: MemberRepository,
    private val financeReportRepository: FinanceReportRepository,
    private val paymentObligationRepository: PaymentObligationRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _state = MutableStateFlow(AppEntryUiState())
    val state: StateFlow<AppEntryUiState> = _state.asStateFlow()
    private var syncJob: Job? = null

    init {
        observeSession()
        bootstrapSession()
    }

    fun consumePendingRoute() {
        _state.update { it.copy(pendingRoute = null) }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _state.update { it.copy(pendingRoute = AppRoutes.LOGIN) }
        }
    }

    fun sendTestNotification() {
        val remoteMessage = RemoteMessage.Builder("test")
            .addData(AppConstants.FCM_NOTIFICATION_TITLE, "Test Notification")
            .addData(AppConstants.FCM_NOTIFICATION_MESSAGE, "Notifikasi test dari halaman profile")
            .addData(AppConstants.FCM_NOTIFICATION_BODY, "Klik untuk cek jalur notifikasi")
            .addData(AppConstants.FCM_NOTIFICATION_ROUTE, AppRoutes.ADMIN_PROFILE)
            .addData(AppConstants.FCM_NOTIFICATION_EVENT_ID, "event-test")
            .build()
        notificationHelper.showNotification(remoteMessage)
    }

    private fun observeSession() {
        viewModelScope.launch {
            observeSessionUseCase().collect { session ->
                _state.update { it.copy(session = session) }
                startOrStopDataSync(session)
            }
        }
    }

    private fun startOrStopDataSync(session: SessionState?) {
        syncJob?.cancel()
        if (session == null) return
        syncJob = viewModelScope.launch {
            while (isActive) {
                syncAllModules(session)
                delay(DATA_SYNC_INTERVAL_MS)
            }
        }
    }

    private suspend fun syncAllModules(session: SessionState) {
        val sectorContext = session.sectorContext
        eventRepository.refresh(sectorContext)
        memberRepository.refresh(sectorContext)
        financeReportRepository.refresh(sectorContext)
        paymentObligationRepository.refresh(sectorContext)
    }

    companion object {
        private const val DATA_SYNC_INTERVAL_MS = 60_000L
    }

    private fun bootstrapSession() {
        viewModelScope.launch {
            val session = bootstrapSessionUseCase()
            _state.update {
                it.copy(
                    session = session,
                    pendingRoute = session?.role?.toHomeRoute() ?: AppRoutes.LOGIN
                )
            }
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
