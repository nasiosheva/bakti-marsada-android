package com.lampung.baktimarsada

import android.app.Application
import com.lampung.baktimarsada.core.constants.AppBuildConfig
import com.lampung.baktimarsada.core.tenant.TenantRuntime
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaktiMarsadaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TenantRuntime.initialize(AppBuildConfig.tenantKey)
    }
}
