package com.lampung.baktimarsada.feature.roulette.model

import com.lampung.baktimarsada.domain.model.MemberDetail
import com.lampung.baktimarsada.domain.model.SessionState

enum class RouletteNameSource {
    MANUAL,
    IMPORT
}

data class RouletteHistoryItem(
    val id: String,
    val winnerName: String,
    val drawnAtMillis: Long
)

data class RoulettePendingSpin(
    val requestId: Long,
    val selectedIndex: Int,
    val selectedName: String,
    val deltaRotationDegrees: Float
)

data class RouletteLocalSnapshot(
    val names: List<String> = emptyList(),
    val history: List<RouletteHistoryItem> = emptyList(),
    val source: RouletteNameSource = RouletteNameSource.MANUAL,
    val lastUpdatedMillis: Long? = null
)

data class RouletteUiState(
    val names: List<String> = emptyList(),
    val history: List<RouletteHistoryItem> = emptyList(),
    val source: RouletteNameSource = RouletteNameSource.MANUAL,
    val lastUpdatedMillis: Long? = null,
    val pendingSpin: RoulettePendingSpin? = null,
    val latestWinner: RouletteHistoryItem? = null,
    val message: String? = null,
    val isLoggingIn: Boolean = false,
    val isLoadingMembers: Boolean = false,
    val members: List<MemberDetail> = emptyList(),
    val session: SessionState? = null
)

// created by Mories Deo Hutapea, S.E.,S.Kom
