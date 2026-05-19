package com.lampung.baktimarsada.feature.roulette.presentation

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RouletteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val participantNames = intent
            ?.getStringArrayListExtra(RouletteIntentContract.EXTRA_PARTICIPANT_NAMES)
            .orEmpty()

        setContent {
            RouletteApp(
                initialNames = participantNames,
                onWinnerConfirmed = { winnerName ->
                    setResult(
                        Activity.RESULT_OK,
                        RouletteLauncher.createResultIntent(winnerName)
                    )
                    finish()
                }
            )
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
