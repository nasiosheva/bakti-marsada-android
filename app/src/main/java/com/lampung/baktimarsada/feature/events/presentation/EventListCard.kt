package com.lampung.baktimarsada.feature.events.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.ui.component.JemaatPill
import com.lampung.baktimarsada.ui.theme.BaktiMarsadaTheme

@Composable
internal fun EventListCard(
    item: EventDetail,
    isAdmin: Boolean,
    onShowDetail: (EventDetail) -> Unit,
    onShowEdit: (EventDetail) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onShowDetail(item) }),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
            ) {
                Spacer(modifier = Modifier.height(96.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.description.isNotBlank()) {
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (isAdmin) {
                        JemaatPill(
                            text = item.sectorName,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EventInfoLine(
                        icon = Icons.Filled.CalendarMonth,
                        text = item.scheduledAt
                    )
                    EventInfoLine(
                        icon = Icons.Filled.LocationOn,
                        text = item.location
                    )
                }
                if (isAdmin) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onShowDetail(item) },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { testTag = "event_detail_${item.id}" },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = stringResource(id = R.string.action_detail),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.action_detail),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        FilledTonalButton(
                            onClick = { onShowEdit(item) },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { testTag = "event_edit_${item.id}" },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(id = R.string.action_edit),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.action_edit),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Button(
                            onClick = { onDelete(item.id) },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { testTag = "event_delete_${item.id}" },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(id = R.string.action_delete),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    FilledTonalButton(
                        onClick = { onShowDetail(item) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = "event_detail_${item.id}" },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.action_detail),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventInfoLine(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

private val previewEventListCardItem = EventDetail(
    id = "ev-1",
    title = "Partangiangan Sektor Mingguan",
    description = "Ibadah sektor bersama keluarga jemaat. Mari datang dan ambil bagian.",
    scheduledAt = "Minggu, 17 Mei 2026 19:00",
    location = "Rumah Keluarga Simanjuntak",
    sectorId = "s-1",
    sectorName = "Sektor 1 HKBP Kedaton",
    programItems = emptyList()
)

private val previewEventListCardLongItem = previewEventListCardItem.copy(
    id = "ev-2",
    title = "Kebaktian Penghiburan Keluarga Besar Hutapea-Simanjuntak Sektor 1",
    description = "Acara penghiburan dan doa syafaat bagi keluarga yang sedang berduka, dihadiri oleh seluruh jemaat sektor dan tamu undangan dari sektor tetangga.",
    scheduledAt = "Sabtu, 23 Mei 2026 18:30",
    location = "Gedung Serbaguna HKBP Kedaton Lampung Selatan"
)

@Preview(name = "EventListCard - Jemaat", showBackground = true, widthDp = 412)
@Composable
private fun EventListCardJemaatPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EventListCard(
                item = previewEventListCardItem,
                isAdmin = false,
                onShowDetail = {},
                onShowEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "EventListCard - Admin", showBackground = true, widthDp = 412)
@Composable
private fun EventListCardAdminPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EventListCard(
                item = previewEventListCardItem,
                isAdmin = true,
                onShowDetail = {},
                onShowEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "EventListCard - Long Content", showBackground = true, widthDp = 412)
@Composable
private fun EventListCardLongPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EventListCard(
                item = previewEventListCardLongItem,
                isAdmin = false,
                onShowDetail = {},
                onShowEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview(
    name = "EventListCard - Admin Dark",
    showBackground = true,
    widthDp = 412,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun EventListCardAdminDarkPreview() {
    BaktiMarsadaTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            EventListCard(
                item = previewEventListCardItem,
                isAdmin = true,
                onShowDetail = {},
                onShowEdit = {},
                onDelete = {}
            )
        }
    }
}

// created by Mories Deo Hutapea, S.E.,S.Kom
