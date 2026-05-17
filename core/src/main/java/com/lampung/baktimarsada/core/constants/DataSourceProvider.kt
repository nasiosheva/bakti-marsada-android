package com.lampung.baktimarsada.core.constants

enum class DataSourceProvider(val value: String) {
    CLOUDFLARE("cloudflare"),
    PYTHON("python"),
    FIREBASE("firebase");

    companion object {
        fun from(raw: String?): DataSourceProvider {
            return entries.firstOrNull { it.value.equals(raw, ignoreCase = true) } ?: CLOUDFLARE
        }
    }
}
