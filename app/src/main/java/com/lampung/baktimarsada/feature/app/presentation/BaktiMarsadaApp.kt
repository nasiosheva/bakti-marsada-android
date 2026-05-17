package com.lampung.baktimarsada.feature.app.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.domain.repository.EventRepository
import com.lampung.baktimarsada.domain.repository.FinanceReportRepository
import com.lampung.baktimarsada.domain.repository.MemberRepository
import com.lampung.baktimarsada.domain.repository.PaymentObligationRepository
import com.lampung.baktimarsada.domain.usecase.BootstrapSessionUseCase
import com.lampung.baktimarsada.domain.usecase.LogoutUseCase
import com.lampung.baktimarsada.domain.usecase.ObserveSessionUseCase
import com.lampung.baktimarsada.feature.app.navigation.AppRoutes
import com.lampung.baktimarsada.feature.auth.presentation.LoginRoute
import com.lampung.baktimarsada.feature.events.presentation.EventCreateRoute
import com.lampung.baktimarsada.feature.events.presentation.EventCopySourceRoute
import com.lampung.baktimarsada.feature.events.presentation.EventEditRoute
import com.lampung.baktimarsada.feature.events.presentation.EventDetailRoute
import com.lampung.baktimarsada.feature.home.presentation.AdminHomeRoute
import com.lampung.baktimarsada.feature.home.presentation.JemaatHomeRoute
import com.lampung.baktimarsada.feature.profile.presentation.ProfileRoute
import dagger.hilt.android.lifecycle.HiltViewModel
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
                    }
                )
            }
        }
        composable(AppRoutes.ADMIN_HOME) {
            state.session?.let { session ->
                AdminHomeRoute(
                    session = session,
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
                    onLogout = { viewModel.logout() }
                )
            }
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
    private val paymentObligationRepository: PaymentObligationRepository
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
