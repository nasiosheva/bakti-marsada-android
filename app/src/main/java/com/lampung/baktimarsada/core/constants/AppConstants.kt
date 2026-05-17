package com.lampung.baktimarsada.core.constants

import com.lampung.baktimarsada.BuildConfig

object AppConstants {
    const val DATABASE_NAME = "bakti_marsada.db"
    const val ENCRYPTED_PREFS_NAME = "bakti_marsada_encrypted_prefs"

    const val KEY_FCM_TOKEN = "key_fcm_token"
    const val KEY_DB_PASSPHRASE = "key_db_passphrase"
    const val KEY_AUTH_TOKEN = "key_auth_token"
    const val KEY_USER_ID = "key_user_id"
    const val KEY_DISPLAY_NAME = "key_display_name"
    const val KEY_USER_ROLE = "key_user_role"
    const val KEY_SECTOR_ID = "key_sector_id"
    const val KEY_SECTOR_NAME = "key_sector_name"

    const val FIRESTORE_COLLECTION_TRACKING = "device_events"
    const val FIRESTORE_FIELD_EVENT_TYPE = "event_type"
    const val FIRESTORE_FIELD_TIMESTAMP = "timestamp"
    const val FIRESTORE_FIELD_SELECTED_ROLE = "selected_role"
    const val FIRESTORE_FIELD_INTEREST_COUNT = "interest_count"
    const val FIRESTORE_FIELD_TOKEN_LENGTH = "token_length"
    const val FIRESTORE_FIELD_MESSAGE = "message"
    const val FIRESTORE_FIELD_SOURCE = "source"
    const val FIRESTORE_FIELD_HAS_DATA = "has_data"
    const val FIRESTORE_FIELD_NOTIFICATION_TITLE = "notification_title"

    const val VALUE_UNKNOWN = "unknown"
    const val VALUE_EMPTY = ""

    const val TRACKING_EVENT_TOKEN_REFRESHED = "fcm_token_refreshed"
    const val TRACKING_EVENT_MESSAGE_RECEIVED = "fcm_message_received"
    const val ROUTE_SYNC_FCM_TOKEN = "v1/device/fcm-token"

    const val DEFAULT_SECTOR_ID = "sector-1"
    const val DEFAULT_SECTOR_NAME = "Sektor 1 HKBP"
    const val SAMPLE_ADMIN_IDENTIFIER = "admin@baktimarsada.id"
    const val SAMPLE_ADMIN_PASSWORD = "admin123"
    const val SAMPLE_JEMAAT_IDENTIFIER = "jemaat@baktimarsada.id"
    const val SAMPLE_JEMAAT_PASSWORD = "jemaat123"

    val NETWORK_BASE_URL: String = BuildConfig.API_BASE_URL
}
