package com.lampung.baktimarsada.feature.events.presentation

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.domain.model.EventDetail
import com.lampung.baktimarsada.domain.model.EventProgramItem
import com.lampung.baktimarsada.domain.model.ProgramItemType
import com.lampung.baktimarsada.domain.model.SessionState
import com.lampung.baktimarsada.domain.model.WorshipTemplate
import com.lampung.baktimarsada.domain.model.WorshipTemplateItem
import com.lampung.baktimarsada.ui.component.BaktiDropdown
import com.lampung.baktimarsada.ui.component.BaktiMultilineInput
import com.lampung.baktimarsada.ui.component.BaktiSectionMessage
import com.lampung.baktimarsada.ui.component.BaktiTextInput
import com.lampung.baktimarsada.ui.component.BaktiToolbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
internal fun EventFormScreen(
    title: String,
    initial: EventDetail?,
    copyFromEvent: EventDetail?,
    session: SessionState,
    templates: List<WorshipTemplate>,
    previousEvents: List<EventDetail>,
    isLoading: Boolean,
    errorMessage: String?,
    onOpenTemplates: () -> Unit,
    onOpenCopyFromPrevious: () -> Unit,
    onBack: () -> Unit,
    onSave: (EventDetail) -> Unit
) {
    var eventTitle by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var schedule by rememberSaveable(initial?.id) { mutableStateOf(initial?.scheduledAt.orEmpty()) }
    var location by rememberSaveable(initial?.id) { mutableStateOf(initial?.location.orEmpty()) }
    var description by rememberSaveable(initial?.id) { mutableStateOf(initial?.description.orEmpty()) }
    var programItems by remember(initial?.id) { mutableStateOf(initial?.programItems?.sortedBy { it.orderIndex }.orEmpty()) }
    val isCreateMode = initial == null
    var showTemplateMenu by rememberSaveable { mutableStateOf(false) }
    var lastCopiedEventId by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(initial?.id) {
        if (initial != null) {
            schedule = initial.scheduledAt
            location = initial.location
            description = initial.description
            eventTitle = initial.title
            programItems = initial.programItems.sortedBy { it.orderIndex }
        }
    }
    LaunchedEffect(copyFromEvent?.id) {
        val source = copyFromEvent ?: return@LaunchedEffect
        if (isCreateMode && source.id != lastCopiedEventId) {
            eventTitle = source.title
            schedule = source.scheduledAt
            location = source.location
            description = source.description
            programItems = source.programItems.mapIndexed { index, item -> item.copy(id = "", eventId = "", orderIndex = index) }
            lastCopiedEventId = source.id
        }
    }

    Scaffold(
        topBar = {
            BaktiToolbar(
                title = title,
                onBack = onBack,
                actions = {
                    if (isCreateMode) {
                        IconButton(onClick = { showTemplateMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(id = R.string.action_manage_templates))
                        }
                        DropdownMenu(expanded = showTemplateMenu, onDismissRequest = { showTemplateMenu = false }) {
                            if (previousEvents.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(id = R.string.action_copy_previous_event)) },
                                    onClick = {
                                        showTemplateMenu = false
                                        onOpenCopyFromPrevious()
                                    }
                                )
                                HorizontalDivider()
                            }
                            templates.forEach { template ->
                                DropdownMenuItem(
                                    text = { Text(text = template.title) },
                                    onClick = {
                                        showTemplateMenu = false
                                        eventTitle = template.title
                                        description = template.description
                                        programItems = template.items.mapIndexed { index, item ->
                                            item.toEventProgramItem(eventId = "", orderIndex = index)
                                        }
                                    }
                                )
                            }
                            if (templates.isNotEmpty()) HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(text = stringResource(id = R.string.action_manage_templates)) },
                                onClick = {
                                    showTemplateMenu = false
                                    onOpenTemplates()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (errorMessage != null) {
                BaktiSectionMessage(message = errorMessage)
            }
            BaktiTextInput(value = eventTitle, label = stringResource(id = R.string.form_title), onValueChange = { eventTitle = it })
            EventDatePickerField(value = schedule, label = stringResource(id = R.string.form_event_date), onDateSelected = { schedule = it })
            BaktiTextInput(value = location, label = stringResource(id = R.string.form_location), onValueChange = { location = it })
            BaktiMultilineInput(value = description, label = stringResource(id = R.string.form_description), onValueChange = { description = it })
            ProgramItemEditor(items = programItems, onItemsChanged = { programItems = it }, emptyLabel = stringResource(id = R.string.program_empty))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), enabled = !isLoading) {
                    Text(text = stringResource(id = R.string.action_cancel))
                }
                Button(
                    onClick = {
                        onSave(
                            EventDetail(
                                id = initial?.id.orEmpty(),
                                title = eventTitle.trim(),
                                description = description.trim(),
                                scheduledAt = schedule.trim(),
                                location = location.trim(),
                                sectorId = session.sectorContext.sectorId,
                                sectorName = session.sectorContext.sectorName,
                                programItems = programItems.normalizedForEvent(initial?.id.orEmpty())
                            )
                        )
                        onBack()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && eventTitle.isNotBlank() && schedule.isNotBlank() && location.isNotBlank()
                ) {
                    Text(text = stringResource(id = R.string.action_save))
                }
            }
        }
    }
}

@Composable
private fun EventDatePickerField(
    value: String,
    label: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val placeholder = stringResource(id = R.string.action_select_date)
    OutlinedButton(
        onClick = {
            val selectedDate = value.toEventDateCalendar()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth -> onDateSelected(formatEventDate(year, month, dayOfMonth)) },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Filled.DateRange, contentDescription = placeholder)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value.ifBlank { placeholder }, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ProgramItemEditor(
    items: List<EventProgramItem>,
    onItemsChanged: (List<EventProgramItem>) -> Unit,
    emptyLabel: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = stringResource(id = R.string.program_section_title), style = MaterialTheme.typography.titleSmall)
        if (items.isEmpty()) {
            Text(text = emptyLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items.sortedBy { it.orderIndex }.forEachIndexed { index, item ->
            ProgramItemCard(
                index = index,
                item = item,
                onChange = { changed -> onItemsChanged(items.replaceAt(index, changed).normalizedOrder()) },
                onMoveUp = { if (index > 0) onItemsChanged(items.move(index, index - 1).normalizedOrder()) },
                onMoveDown = { if (index < items.lastIndex) onItemsChanged(items.move(index, index + 1).normalizedOrder()) },
                onDelete = { onItemsChanged(items.filterIndexed { itemIndex, _ -> itemIndex != index }.normalizedOrder()) }
            )
        }
        OutlinedButton(
            onClick = {
                onItemsChanged(
                    (items + EventProgramItem(id = "", eventId = "", orderIndex = items.size, title = "", content = "", leader = "", type = ProgramItemType.CUSTOM))
                        .normalizedOrder()
                )
            }
        ) {
            Text(text = stringResource(id = R.string.action_add_program_item))
        }
    }
}

@Composable
private fun ProgramItemCard(
    index: Int,
    item: EventProgramItem,
    onChange: (EventProgramItem) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(id = R.string.program_item_number, index + 1), style = MaterialTheme.typography.labelLarge)
            val typeOptions = listOf(
                ProgramItemType.OPENING to stringResource(id = R.string.program_type_opening),
                ProgramItemType.SONG to stringResource(id = R.string.program_type_song),
                ProgramItemType.PRAYER to stringResource(id = R.string.program_type_prayer),
                ProgramItemType.SCRIPTURE to stringResource(id = R.string.program_type_scripture),
                ProgramItemType.SERMON to stringResource(id = R.string.program_type_sermon),
                ProgramItemType.OFFERING to stringResource(id = R.string.program_type_offering),
                ProgramItemType.ANNOUNCEMENT to stringResource(id = R.string.program_type_announcement),
                ProgramItemType.CLOSING to stringResource(id = R.string.program_type_closing),
                ProgramItemType.CUSTOM to stringResource(id = R.string.program_type_custom)
            )
            BaktiDropdown(
                selectedValue = typeOptions.firstOrNull { it.first == item.type }?.second.orEmpty(),
                label = stringResource(id = R.string.form_program_type),
                options = typeOptions.map { it.second },
                onValueSelected = { selected ->
                    onChange(item.copy(type = typeOptions.firstOrNull { it.second == selected }?.first ?: ProgramItemType.CUSTOM))
                }
            )
            when (item.type) {
                ProgramItemType.SCRIPTURE -> {
                    BaktiTextInput(value = item.scriptureReference, label = stringResource(id = R.string.form_program_scripture_reference), onValueChange = { onChange(item.copy(scriptureReference = it)) })
                    BaktiMultilineInput(value = item.scriptureText, label = stringResource(id = R.string.form_program_scripture_text), onValueChange = { onChange(item.copy(scriptureText = it, content = it)) })
                }
                ProgramItemType.SERMON -> BaktiTextInput(value = item.title, label = stringResource(id = R.string.form_program_title), onValueChange = { onChange(item.copy(title = it)) })
                ProgramItemType.SONG -> {
                    BaktiTextInput(value = item.title, label = stringResource(id = R.string.form_program_song_title), onValueChange = { onChange(item.copy(title = it)) })
                    BaktiMultilineInput(value = item.content, label = stringResource(id = R.string.form_program_song_content), onValueChange = { onChange(item.copy(content = it)) })
                }
                ProgramItemType.PRAYER -> {
                    BaktiTextInput(value = item.title, label = stringResource(id = R.string.form_program_title), onValueChange = { onChange(item.copy(title = it)) })
                    BaktiTextInput(value = item.leader, label = stringResource(id = R.string.form_program_prayer_leader), onValueChange = { onChange(item.copy(leader = it)) })
                    BaktiMultilineInput(value = item.content, label = stringResource(id = R.string.form_program_prayer_content), onValueChange = { onChange(item.copy(content = it)) })
                }
                ProgramItemType.ANNOUNCEMENT -> {
                    BaktiTextInput(value = item.title, label = stringResource(id = R.string.form_program_title), onValueChange = { onChange(item.copy(title = it)) })
                    BaktiMultilineInput(value = item.content, label = stringResource(id = R.string.form_program_announcement_content), onValueChange = { onChange(item.copy(content = it)) })
                }
                ProgramItemType.OFFERING -> {
                    BaktiTextInput(value = item.title, label = stringResource(id = R.string.form_program_title), onValueChange = { onChange(item.copy(title = it)) })
                    BaktiTextInput(value = item.leader, label = stringResource(id = R.string.form_program_leader), onValueChange = { onChange(item.copy(leader = it)) })
                    BaktiMultilineInput(value = item.note, label = stringResource(id = R.string.form_program_note), onValueChange = { onChange(item.copy(note = it)) })
                }
                ProgramItemType.OPENING, ProgramItemType.CLOSING, ProgramItemType.CUSTOM -> {
                    BaktiTextInput(value = item.title, label = stringResource(id = R.string.form_program_title), onValueChange = { onChange(item.copy(title = it)) })
                    BaktiTextInput(value = item.leader, label = stringResource(id = R.string.form_program_leader), onValueChange = { onChange(item.copy(leader = it)) })
                    BaktiMultilineInput(value = item.content, label = stringResource(id = R.string.form_program_content), onValueChange = { onChange(item.copy(content = it)) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onMoveUp, enabled = index > 0) { Text(text = stringResource(id = R.string.action_move_up)) }
                OutlinedButton(onClick = onMoveDown) { Text(text = stringResource(id = R.string.action_move_down)) }
                TextButton(onClick = onDelete) { Text(text = stringResource(id = R.string.action_delete)) }
            }
        }
    }
}

@Composable
internal fun EventTemplateFormScreen(
    screenTitle: String,
    initial: WorshipTemplate?,
    session: SessionState,
    onBack: () -> Unit,
    onSave: (WorshipTemplate) -> Unit
) {
    var title by rememberSaveable(initial?.id) { mutableStateOf(initial?.title.orEmpty()) }
    var description by rememberSaveable(initial?.id) { mutableStateOf(initial?.description.orEmpty()) }
    var programItems by remember(initial?.id) {
        mutableStateOf(
            initial?.items
                ?.map { templateItem ->
                    templateItem.toEventProgramItem(
                        eventId = "",
                        orderIndex = templateItem.orderIndex
                    )
                }
                .orEmpty()
        )
    }

    Scaffold(topBar = { BaktiToolbar(title = screenTitle, onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BaktiTextInput(value = title, label = stringResource(id = R.string.form_title), onValueChange = { title = it })
            BaktiMultilineInput(value = description, label = stringResource(id = R.string.form_description), onValueChange = { description = it })
            ProgramItemEditor(items = programItems, onItemsChanged = { programItems = it }, emptyLabel = stringResource(id = R.string.template_program_empty))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text(text = stringResource(id = R.string.action_cancel)) }
                Button(
                    onClick = {
                        val templateId = initial?.id.orEmpty()
                        onSave(
                            WorshipTemplate(
                                id = templateId,
                                tenantId = session.tenantContext.tenantId,
                                sectorId = session.sectorContext.sectorId,
                                title = title.trim(),
                                description = description.trim(),
                                items = programItems.normalizedForTemplate(templateId)
                            )
                        )
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.action_save))
                }
            }
        }
    }
}

private fun String.toEventDateCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    val dateValue = trim().take(AppConstants.DATE_FORMAT_ISO_LOCAL_DATE.length)
    val parsedDate = runCatching {
        SimpleDateFormat(AppConstants.DATE_FORMAT_ISO_LOCAL_DATE, Locale.US).parse(dateValue)
    }.getOrNull()
    if (parsedDate != null) {
        calendar.time = parsedDate
    }
    return calendar
}

private fun formatEventDate(year: Int, month: Int, dayOfMonth: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, dayOfMonth)
    }
    return SimpleDateFormat(AppConstants.DATE_FORMAT_ISO_LOCAL_DATE, Locale.US).format(calendar.time)
}

private fun List<EventProgramItem>.replaceAt(index: Int, item: EventProgramItem): List<EventProgramItem> {
    return mapIndexed { itemIndex, current -> if (itemIndex == index) item else current }
}

private fun List<EventProgramItem>.move(fromIndex: Int, toIndex: Int): List<EventProgramItem> {
    return toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
}

private fun List<EventProgramItem>.normalizedOrder(): List<EventProgramItem> {
    return mapIndexed { index, item -> item.copy(orderIndex = index) }
}

private fun List<EventProgramItem>.normalizedForEvent(eventId: String): List<EventProgramItem> {
    return normalizedOrder().mapIndexedNotNull { index, item ->
        if (item.isBlankProgramItem()) null
        else item.copy(
            id = item.id.ifBlank { if (eventId.isBlank()) "" else "$eventId-program-$index" },
            eventId = eventId,
            orderIndex = index,
            title = item.title.trim(),
            content = item.content.trim(),
            leader = item.leader.trim(),
            scriptureReference = item.scriptureReference.trim(),
            scriptureText = item.scriptureText.trim(),
            note = item.note.trim()
        )
    }
}

private fun List<EventProgramItem>.normalizedForTemplate(templateId: String): List<WorshipTemplateItem> {
    return normalizedOrder().mapIndexedNotNull { index, item ->
        if (item.isBlankProgramItem()) null
        else WorshipTemplateItem(
            id = item.id.ifBlank { if (templateId.isBlank()) "" else "$templateId-item-$index" },
            templateId = templateId,
            orderIndex = index,
            title = item.title.trim(),
            content = item.content.trim(),
            leader = item.leader.trim(),
            type = item.type,
            scriptureReference = item.scriptureReference.trim(),
            scriptureText = item.scriptureText.trim(),
            note = item.note.trim()
        )
    }
}

private fun EventProgramItem.isBlankProgramItem(): Boolean {
    return title.isBlank() &&
        content.isBlank() &&
        leader.isBlank() &&
        scriptureReference.isBlank() &&
        scriptureText.isBlank() &&
        note.isBlank()
}

// created by Mories Deo Hutapea, S.E.,S.Kom
