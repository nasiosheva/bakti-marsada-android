package com.lampung.baktimarsada.feature.app.presentation

import com.lampung.baktimarsada.domain.model.UserRole
import com.lampung.baktimarsada.domain.usecase.BootstrapSessionUseCase
import com.lampung.baktimarsada.domain.usecase.LogoutUseCase
import com.lampung.baktimarsada.domain.usecase.ObserveSessionUseCase
import com.lampung.baktimarsada.feature.app.navigation.AppRoutes
import com.lampung.baktimarsada.test.MainDispatcherRule
import com.lampung.baktimarsada.test.fakes.FakeAuthRepository
import com.lampung.baktimarsada.test.fakes.sampleSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppEntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `bootstrap without session navigates to login`() = runTest {
        val repository = FakeAuthRepository(initialSession = null)

        val viewModel = AppEntryViewModel(
            observeSessionUseCase = ObserveSessionUseCase(repository),
            bootstrapSessionUseCase = BootstrapSessionUseCase(repository),
            logoutUseCase = LogoutUseCase(repository)
        )
        advanceUntilIdle()

        assertEquals(AppRoutes.LOGIN, viewModel.state.value.pendingRoute)
        assertNull(viewModel.state.value.session)
    }

    @Test
    fun `bootstrap with admin session navigates to admin home`() = runTest {
        val repository = FakeAuthRepository(initialSession = sampleSession(UserRole.ADMIN))

        val viewModel = AppEntryViewModel(
            observeSessionUseCase = ObserveSessionUseCase(repository),
            bootstrapSessionUseCase = BootstrapSessionUseCase(repository),
            logoutUseCase = LogoutUseCase(repository)
        )
        advanceUntilIdle()

        assertEquals(AppRoutes.ADMIN_HOME, viewModel.state.value.pendingRoute)
        assertEquals(UserRole.ADMIN, viewModel.state.value.session?.role)
    }

    @Test
    fun `logout clears session and returns to login`() = runTest {
        val repository = FakeAuthRepository(initialSession = sampleSession(UserRole.JEMAAT))
        val viewModel = AppEntryViewModel(
            observeSessionUseCase = ObserveSessionUseCase(repository),
            bootstrapSessionUseCase = BootstrapSessionUseCase(repository),
            logoutUseCase = LogoutUseCase(repository)
        )
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        assertEquals(AppRoutes.LOGIN, viewModel.state.value.pendingRoute)
        assertNull(viewModel.state.value.session)
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
