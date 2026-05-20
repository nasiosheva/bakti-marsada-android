package com.lampung.baktimarsada.feature.roulette.data

import com.lampung.baktimarsada.core.result.AppResult
import com.lampung.baktimarsada.feature.roulette.model.RouletteMemberUi
import com.lampung.baktimarsada.feature.roulette.model.RouletteSessionUi
import kotlinx.coroutines.flow.Flow

interface RouletteImportDataSource {
    fun observeSession(): Flow<RouletteSessionUi?>

    fun observeMembers(): Flow<List<RouletteMemberUi>>

    suspend fun login(identifier: String, password: String): AppResult<Unit>

    suspend fun refreshMembers(): AppResult<Unit>
}

// created by Mories Deo Hutapea, S.E.,S.Kom
