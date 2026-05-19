package com.lampung.baktimarsada.feature.roulette.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lampung.baktimarsada.feature.roulette.model.RouletteHistoryItem
import com.lampung.baktimarsada.feature.roulette.model.RouletteNameSource
import com.lampung.baktimarsada.feature.roulette.model.RouletteUiState
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

@Preview(showBackground = true)
@Composable
private fun RouletteHomePreview() {
    BaktiMarsadaTheme {
        RouletteHomeContent(
            state = RouletteUiState(
                names = listOf("Mories Deo", "S. Simanjuntak", "R. Naibaho", "T. Sitorus"),
                history = listOf(
                    RouletteHistoryItem(
                        id = "1",
                        winnerName = "Mories Deo",
                        drawnAtMillis = System.currentTimeMillis()
                    )
                ),
                source = RouletteNameSource.MANUAL,
                lastUpdatedMillis = System.currentTimeMillis()
            ),
            innerPadding = PaddingValues(0.dp),
            onSpinRequested = { true },
            onSpinAnimationCompleted = {},
            onEditNames = {},
            onImportMembers = {},
            onClearHistory = {}
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
