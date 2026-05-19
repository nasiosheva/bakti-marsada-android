package com.lampung.baktimarsada.feature.roulette.model

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

data class RouletteMemberUi(
    val id: String,
    val fullName: String,
    val familyGroup: String,
    val roleInSector: String
)

data class RouletteSessionUi(
    val userId: String,
    val displayName: String,
    val sectorName: String
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
    val members: List<RouletteMemberUi> = emptyList(),
    val session: RouletteSessionUi? = null
)

// created by Mories Deo Hutapea, S.E.,S.Kom
