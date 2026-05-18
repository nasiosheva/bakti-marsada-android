package com.lampung.baktimarsada

import android.app.Application
import com.lampung.baktimarsada.core.constants.AppBuildConfig
import com.lampung.baktimarsada.core.tenant.TenantRuntime
import com.lampung.baktimarsada.firebase.core.remoteconfig.RemoteConfigBootstrap
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BaktiMarsadaApplication : Application() {
    @Inject
    lateinit var remoteConfigBootstrap: RemoteConfigBootstrap

    override fun onCreate() {
        super.onCreate()
        TenantRuntime.initialize(AppBuildConfig.tenantKey)
        remoteConfigBootstrap.preload()
    }
}
