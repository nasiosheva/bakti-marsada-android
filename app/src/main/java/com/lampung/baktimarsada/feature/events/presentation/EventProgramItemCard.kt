package com.lampung.baktimarsada.feature.events.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.domain.model.EventProgramItem
import com.lampung.baktimarsada.domain.model.ProgramItemType
import com.lampung.baktimarsada.ui.component.JemaatPill
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

@Composable
fun EventProgramItemCard(
    index: Int,
    programItem: EventProgramItem,
    modifier: Modifier = Modifier,
    showIndexBadge: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JemaatPill(text = programItem.type.programTypeLabel())
                Box(modifier = Modifier.weight(1f))
                if (showIndexBadge) {
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

private val previewOpening = EventProgramItem(
    id = "p-1",
    eventId = "e-1",
    orderIndex = 0,
    title = "Pembukaan",
    content = "Salam dan pengantar singkat dari liturgis sektor.",
    leader = "Liturgis",
    type = ProgramItemType.OPENING
)

private val previewScripture = EventProgramItem(
    id = "p-2",
    eventId = "e-1",
    orderIndex = 1,
    title = "Pembacaan Firman",
    content = "",
    leader = "Pemimpin Ibadah",
    type = ProgramItemType.SCRIPTURE,
    scriptureReference = "Mazmur 23:1-4",
    scriptureText = "Tuhan adalah gembalaku, takkan kekurangan aku."
)

private val previewOffering = EventProgramItem(
    id = "p-3",
    eventId = "e-1",
    orderIndex = 2,
    title = "Persembahan",
    content = "Persembahan ucapan syukur jemaat.",
    leader = "Bendahara Sektor",
    type = ProgramItemType.OFFERING,
    note = "Dikumpulkan oleh penatua sektor."
)

private val previewAnnouncement = EventProgramItem(
    id = "p-4",
    eventId = "e-1",
    orderIndex = 3,
    title = "Pengumuman",
    content = "Latihan koor sektor hari Rabu pukul 19.30.",
    leader = "",
    type = ProgramItemType.ANNOUNCEMENT
)

@Preview(name = "Program Item - Opening (with badge)", showBackground = true)
@Composable
private fun EventProgramItemCardOpeningPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EventProgramItemCard(index = 0, programItem = previewOpening)
        }
    }
}

@Preview(name = "Program Item - Scripture", showBackground = true)
@Composable
private fun EventProgramItemCardScripturePreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EventProgramItemCard(index = 1, programItem = previewScripture)
        }
    }
}

@Preview(name = "Program Item - Offering", showBackground = true)
@Composable
private fun EventProgramItemCardOfferingPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EventProgramItemCard(index = 2, programItem = previewOffering)
        }
    }
}

@Preview(name = "Program Item - Announcement (no badge)", showBackground = true)
@Composable
private fun EventProgramItemCardAnnouncementNoBadgePreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EventProgramItemCard(
                index = 3,
                programItem = previewAnnouncement,
                showIndexBadge = false
            )
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
