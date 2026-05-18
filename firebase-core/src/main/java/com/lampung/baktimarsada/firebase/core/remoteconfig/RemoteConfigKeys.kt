package com.lampung.baktimarsada.firebase.core.remoteconfig

object RemoteConfigKeys {
    val homeBannerEnabled = RemoteConfigKey.boolean("app.home_banner_enabled")
    val homeBannerTitle = RemoteConfigKey.string("app.home_banner_title")
    val minimumSupportedVersionCode = RemoteConfigKey.long(
        baseKey = "app.minimum_supported_version_code",
        tenantAware = false
    )
    val eventHighlightEnabled = RemoteConfigKey.boolean("app.event_highlight_enabled")
}
