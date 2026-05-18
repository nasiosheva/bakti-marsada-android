package com.lampung.baktimarsada.feature.events.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.lampung.baktimarsada.R
import com.lampung.baktimarsada.core.constants.AppConstants
import com.lampung.baktimarsada.domain.model.EventDetail
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal data class EventSection(
    val title: String,
    val items: List<EventDetail>
)

@Composable
internal fun rememberJemaatEventSections(items: List<EventDetail>): List<EventSection> {
    val weekTitle = stringResource(id = R.string.event_section_this_week)
    val monthTitle = stringResource(id = R.string.event_section_this_month)
    val moreThanMonthTitle = stringResource(id = R.string.event_section_above_one_month)
    return buildEventSections(
        items = items,
        weekTitle = weekTitle,
        monthTitle = monthTitle,
        moreThanMonthTitle = moreThanMonthTitle
    )
}

private fun buildEventSections(
    items: List<EventDetail>,
    weekTitle: String,
    monthTitle: String,
    moreThanMonthTitle: String
): List<EventSection> {
    val today = Calendar.getInstance().startOfDay()
    val inAWeek = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 7) }
    val inAMonth = (today.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
    val weekItems = mutableListOf<EventDetail>()
    val monthItems = mutableListOf<EventDetail>()
    val moreThanMonthItems = mutableListOf<EventDetail>()

    items.forEach { item ->
        val eventDate = item.scheduledAt.toEventCalendar() ?: run {
            moreThanMonthItems += item
            return@forEach
        }
        when {
            !eventDate.before(today) && !eventDate.after(inAWeek) -> weekItems += item
            eventDate.after(inAWeek) && !eventDate.after(inAMonth) -> monthItems += item
            else -> moreThanMonthItems += item
        }
    }

    return listOf(
        EventSection(weekTitle, weekItems),
        EventSection(monthTitle, monthItems),
        EventSection(moreThanMonthTitle, moreThanMonthItems)
    ).filter { it.items.isNotEmpty() }
}

private fun String.toEventCalendar(): Calendar? {
    val datePortion = trim().take(AppConstants.DATE_FORMAT_ISO_LOCAL_DATE.length)
    val parsedDate = runCatching {
        SimpleDateFormat(AppConstants.DATE_FORMAT_ISO_LOCAL_DATE, Locale.US).parse(datePortion)
    }.getOrNull() ?: return null
    return Calendar.getInstance().apply { time = parsedDate }.startOfDay()
}

private fun Calendar.startOfDay(): Calendar {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return this
}

internal fun EventDetail.matchesEventQuery(query: String): Boolean {
    val keyword = query.trim().lowercase()
    if (keyword.isBlank()) return true
    return title.lowercase().contains(keyword) ||
        description.lowercase().contains(keyword) ||
        location.lowercase().contains(keyword) ||
        scheduledAt.lowercase().contains(keyword) ||
        sectorName.lowercase().contains(keyword)
}

// created by Mories Deo Hutapea, S.E.,S.Kom
