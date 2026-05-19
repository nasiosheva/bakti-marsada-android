package com.lampung.baktimarsada.feature.roulette.presentation

import com.lampung.baktimarsada.core.resources.StringProvider
import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.feature.roulette.R
import com.lampung.baktimarsada.feature.roulette.data.RouletteImportDataSource
import com.lampung.baktimarsada.feature.roulette.data.RouletteLocalDataSource
import com.lampung.baktimarsada.feature.roulette.model.RouletteLocalSnapshot
import com.lampung.baktimarsada.feature.roulette.model.RouletteMemberUi
import com.lampung.baktimarsada.feature.roulette.model.RouletteNameSource
import com.lampung.baktimarsada.feature.roulette.model.RouletteSessionUi
import com.lampung.baktimarsada.test.MainDispatcherRule
import com.lampung.baktimarsada.test.fakes.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RouletteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `requestSpin returns false when names empty`() = runTest {
        val viewModel = RouletteViewModel(
            localStore = FakeLocalDataSource(),
            importDataSource = FakeImportDataSource(),
            stringProvider = FakeStringProvider(),
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)
        )
        advanceUntilIdle()

        val result = viewModel.requestSpin(0f)

        assertFalse(result)
        assertEquals("Tambahkan nama", viewModel.state.value.message)
    }

    @Test
    fun `saveManualNames updates local store source manual`() = runTest {
        val localStore = FakeLocalDataSource()
        val viewModel = RouletteViewModel(
            localStore = localStore,
            importDataSource = FakeImportDataSource(),
            stringProvider = FakeStringProvider(),
            dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)
        )

        viewModel.saveManualNames("Andi\nBudi")
        advanceUntilIdle()

        assertEquals(listOf("Andi", "Budi"), localStore.lastSavedNames)
        assertEquals(RouletteNameSource.MANUAL, localStore.lastSource)
        assertTrue(viewModel.state.value.message?.isNotBlank() == true)
    }

    private class FakeStringProvider : StringProvider {
        override fun get(resId: Int): String {
            return when (resId) {
                R.string.roulette_spin_requires_names -> "Tambahkan nama"
                R.string.roulette_names_saved -> "Tersimpan"
                R.string.roulette_history_cleared -> "Riwayat dihapus"
                R.string.roulette_import_no_selection -> "Pilih peserta"
                R.string.roulette_import_success -> "Impor sukses"
                R.string.roulette_login_identifier_required -> "Identifier wajib"
                R.string.roulette_login_password_required -> "Password wajib"
                R.string.roulette_login_success -> "Login sukses"
                else -> "string-$resId"
            }
        }

        override fun get(resId: Int, vararg args: Any): String = get(resId)
    }

    private class FakeLocalDataSource : RouletteLocalDataSource {
        private val snapshots = MutableStateFlow(RouletteLocalSnapshot())
        var lastSavedNames: List<String> = emptyList()
        var lastSource: RouletteNameSource? = null

        override val snapshot: Flow<RouletteLocalSnapshot> = snapshots

        override suspend fun replaceNames(names: List<String>, source: RouletteNameSource) {
            lastSavedNames = names
            lastSource = source
            snapshots.value = snapshots.value.copy(names = names, source = source)
        }

        override suspend fun appendHistory(winnerName: String) = Unit

        override suspend fun clearHistory() = Unit
    }

    private class FakeImportDataSource : RouletteImportDataSource {
        override fun observeSession(): Flow<RouletteSessionUi?> = MutableStateFlow(null)
        override fun observeMembers(): Flow<List<RouletteMemberUi>> = MutableStateFlow(emptyList())
        override suspend fun login(identifier: String, password: String): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun logout() = Unit
        override suspend fun refreshMembers(): AppResult<Unit> = AppResult.Success(Unit)
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
