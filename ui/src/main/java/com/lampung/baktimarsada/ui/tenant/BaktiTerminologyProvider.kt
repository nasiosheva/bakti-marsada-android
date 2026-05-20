package com.lampung.baktimarsada.ui.tenant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Wrap UI subtrees that should resolve [LocalTerminology] from a tenant-provided value.
 * Pass [BaktiTerminology.Default] (or a snapshot mapped from `TenantProfileSnapshot.terminology`)
 * to override the defaults. Empty fields fall back to defaults to avoid blank labels on screen.
 */
@Composable
fun BaktiTerminologyProvider(
    terminology: BaktiTerminology,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalTerminology provides terminology.withDefaultsForBlankFields()
    ) {
        content()
    }
}

private fun BaktiTerminology.withDefaultsForBlankFields(): BaktiTerminology = copy(
    sectorLabel = sectorLabel.ifBlank { BaktiTerminology.Default.sectorLabel },
    sectorPluralLabel = sectorPluralLabel.ifBlank { BaktiTerminology.Default.sectorPluralLabel },
    gatheringLabel = gatheringLabel.ifBlank { BaktiTerminology.Default.gatheringLabel },
    memberLabel = memberLabel.ifBlank { BaktiTerminology.Default.memberLabel },
    financeLabel = financeLabel.ifBlank { BaktiTerminology.Default.financeLabel },
    paymentLabel = paymentLabel.ifBlank { BaktiTerminology.Default.paymentLabel },
    arisanLabel = arisanLabel.ifBlank { BaktiTerminology.Default.arisanLabel }
)

// created by Mories Deo Hutapea, S.E.,S.Kom
