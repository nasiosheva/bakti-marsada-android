package com.lampung.baktimarsada.feature.roulette.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult

object RouletteLauncher {
    fun createIntent(
        context: Context,
        participantNames: List<String>
    ): Intent {
        return Intent(context, RouletteActivity::class.java).apply {
            putStringArrayListExtra(
                RouletteIntentContract.EXTRA_PARTICIPANT_NAMES,
                ArrayList(participantNames)
            )
        }
    }

    fun parseWinnerName(result: ActivityResult): String? {
        if (result.resultCode != Activity.RESULT_OK) return null
        return result.data
            ?.getStringExtra(RouletteIntentContract.EXTRA_SELECTED_WINNER_NAME)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    fun createResultIntent(winnerName: String): Intent {
        return Intent().putExtra(
            RouletteIntentContract.EXTRA_SELECTED_WINNER_NAME,
            winnerName
        )
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
