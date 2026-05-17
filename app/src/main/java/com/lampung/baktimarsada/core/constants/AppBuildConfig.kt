package com.lampung.baktimarsada.core.constants

import com.lampung.baktimarsada.BuildConfig

object AppBuildConfig {
    val databaseName: String = BuildConfig.DATABASE_NAME
    val tenantKey: String = BuildConfig.TENANT_KEY
    val cloudflareBaseUrl: String = BuildConfig.API_BASE_URL_CLOUDFLARE
    val pythonBaseUrl: String = BuildConfig.API_BASE_URL_PYTHON
    val dataSourceProvider: DataSourceProvider = DataSourceProvider.from(BuildConfig.DATA_SOURCE_PROVIDER)
    val dataSourceLabel: String = dataSourceProvider.value
    val appEnvironment: String = BuildConfig.APP_ENVIRONMENT
    val simulationEnabled: Boolean = BuildConfig.SIMULATION_ENABLED
}

// created by Mories Deo Hutapea, S.E.,S.Kom
