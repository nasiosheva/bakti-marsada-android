package com.lampung.baktimarsada.ui.tenant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Per-tenant terminology, runtime-bound. UI calls [LocalTerminology.current.sectorLabel] in place
 * of `stringResource(R.string.sector_label_default)` to render the right word for the active
 * church (e.g. "Sektor" for HKBP vs "Lingkungan" for Katolik).
 *
 * Apply via [BaktiTerminologyProvider] at the root of the app once the tenant profile snapshot
 * is loaded. When no tenant profile is available yet, [DefaultBaktiTerminology] is used — those
 * defaults match the historical Indonesian copy already in `R.string`.
 */
data class BaktiTerminology(
    val sectorLabel: String,
    val sectorPluralLabel: String,
    val gatheringLabel: String,
    val memberLabel: String,
    val financeLabel: String,
    val paymentLabel: String,
    val arisanLabel: String
) {
    companion object {
        val Default: BaktiTerminology = BaktiTerminology(
            sectorLabel = "Sektor",
            sectorPluralLabel = "Sektor",
            gatheringLabel = "Partangiangan",
            memberLabel = "Anggota Jemaat",
            financeLabel = "Laporan Keuangan",
            paymentLabel = "Tagihan",
            arisanLabel = "Arisan"
        )
    }
}

val LocalTerminology = staticCompositionLocalOf { BaktiTerminology.Default }

/**
 * Convenience accessor — `BaktiTerminologies.current.sectorLabel` reads from the
 * [LocalTerminology] composition local without an extra import in calling code.
 */
object BaktiTerminologies {
    val current: BaktiTerminology
        @Composable
        @ReadOnlyComposable
        get() = LocalTerminology.current
}

// created by Mories Deo Hutapea, S.E.,S.Kom
