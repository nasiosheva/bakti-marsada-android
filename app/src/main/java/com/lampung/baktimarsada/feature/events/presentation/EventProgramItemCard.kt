package com.lampung.baktimarsada.feature.events.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.domain.model.EventProgramItem
import com.lampung.baktimarsada.domain.model.ProgramItemType
import com.lampung.baktimarsada.ui.component.JemaatPill

@Composable
fun EventProgramItemCard(
    index: Int,
    programItem: EventProgramItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JemaatPill(text = programItem.type.programTypeLabel())
                Box(modifier = Modifier.weight(1f))
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            if (programItem.title.isNotBlank()) {
                Text(
                    text = programItem.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (programItem.leader.isNotBlank()) {
                Text(
                    text = programItem.leader,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            EventProgramItemBody(programItem = programItem)
        }
    }
}

@Composable
private fun EventProgramItemBody(programItem: EventProgramItem) {
    val bodyText = when (programItem.type) {
        ProgramItemType.SCRIPTURE -> listOf(
            programItem.scriptureReference,
            programItem.scriptureText.ifBlank { programItem.content }
        ).filter { it.isNotBlank() }.joinToString(separator = "\n")
        ProgramItemType.OFFERING -> programItem.note.ifBlank { programItem.content }
        else -> programItem.content
    }
    if (bodyText.isNotBlank()) {
        Text(
            text = bodyText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProgramItemType.programTypeLabel(): String {
    return when (this) {
        ProgramItemType.OPENING -> stringResource(id = R.string.program_type_opening)
        ProgramItemType.SONG -> stringResource(id = R.string.program_type_song)
        ProgramItemType.PRAYER -> stringResource(id = R.string.program_type_prayer)
        ProgramItemType.SCRIPTURE -> stringResource(id = R.string.program_type_scripture)
        ProgramItemType.SERMON -> stringResource(id = R.string.program_type_sermon)
        ProgramItemType.OFFERING -> stringResource(id = R.string.program_type_offering)
        ProgramItemType.ANNOUNCEMENT -> stringResource(id = R.string.program_type_announcement)
        ProgramItemType.CLOSING -> stringResource(id = R.string.program_type_closing)
        ProgramItemType.CUSTOM -> stringResource(id = R.string.program_type_custom)
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
