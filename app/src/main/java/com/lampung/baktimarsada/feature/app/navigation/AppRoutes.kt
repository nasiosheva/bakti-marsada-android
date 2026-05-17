package com.lampung.baktimarsada.feature.app.navigation

object AppRoutes {
    const val EVENT_ID_ARG = "eventId"
    const val SOURCE_EVENT_ID_ARG = "sourceEventId"

    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val JEMAAT_HOME = "jemaat_home"
    const val ADMIN_HOME = "admin_home"
    const val JEMAAT_EVENT_DETAIL = "jemaat_event_detail/{$EVENT_ID_ARG}"
    const val ADMIN_EVENT_DETAIL = "admin_event_detail/{$EVENT_ID_ARG}"

    const val JEMAAT_EVENTS = "jemaat_events"
    const val JEMAAT_MEMBERS = "jemaat_members"
    const val JEMAAT_FINANCE = "jemaat_finance"
    const val JEMAAT_PAYMENTS = "jemaat_payments"
    const val JEMAAT_PROFILE = "jemaat_profile"

    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_EVENTS = "admin_events"
    const val ADMIN_EVENT_CREATE = "admin_event_create"
    const val ADMIN_EVENT_CREATE_FROM = "admin_event_create/{$SOURCE_EVENT_ID_ARG}"
    const val ADMIN_EVENT_COPY_SOURCE = "admin_event_copy_source"
    const val ADMIN_EVENT_EDIT = "admin_event_edit/{$EVENT_ID_ARG}"
    const val ADMIN_MEMBERS = "admin_members"
    const val ADMIN_FINANCE = "admin_finance"
    const val ADMIN_PAYMENTS = "admin_payments"
    const val ADMIN_PROFILE = "admin_profile"

    fun jemaatEventDetail(eventId: String): String = "jemaat_event_detail/$eventId"

    fun adminEventDetail(eventId: String): String = "admin_event_detail/$eventId"

    fun adminEventEdit(eventId: String): String = "admin_event_edit/$eventId"

    fun adminEventCreateFrom(sourceEventId: String): String = "admin_event_create/$sourceEventId"
}

// created by Mories Deo Hutapea, S.E.,S.Kom
