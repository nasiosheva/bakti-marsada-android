package com.lampung.baktimarsada.feature.roulette.data

import com.lampung.baktimarsada.feature.roulette.model.RouletteLocalSnapshot
import com.lampung.baktimarsada.feature.roulette.model.RouletteNameSource
import kotlinx.coroutines.flow.Flow

interface RouletteLocalDataSource {
    val snapshot: Flow<RouletteLocalSnapshot>

    suspend fun replaceNames(names: List<String>, source: RouletteNameSource)

    suspend fun appendHistory(winnerName: String)

    suspend fun clearHistory()
}

// created by Mories Deo Hutapea, S.E.,S.Kom
