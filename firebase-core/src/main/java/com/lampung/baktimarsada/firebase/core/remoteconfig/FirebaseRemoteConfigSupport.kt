package com.lampung.baktimarsada.firebase.core.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.lampung.baktimarsada.core.dispatchers.DispatcherProvider
import com.lampung.baktimarsada.core.tenant.TenantRuntime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface RemoteConfigClient {
    suspend fun configure(defaultsResId: Int, minimumFetchIntervalSeconds: Long)
    suspend fun fetchAndActivate(): Boolean
    suspend fun activate(): Boolean
    fun getString(key: String): String
    fun getValueSource(key: String): Int
}

class FirebaseRemoteConfigClient @Inject constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig?
) : RemoteConfigClient {

    @Volatile
    private var configuredDefaultsResId: Int? = null

    @Volatile
    private var configuredFetchIntervalSeconds: Long? = null

    override suspend fun configure(defaultsResId: Int, minimumFetchIntervalSeconds: Long) {
        val remoteConfig = firebaseRemoteConfig ?: return
        if (configuredDefaultsResId == defaultsResId &&
            configuredFetchIntervalSeconds == minimumFetchIntervalSeconds
        ) {
            return
        }
        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(minimumFetchIntervalSeconds)
                .build()
        ).await()
        remoteConfig.setDefaultsAsync(defaultsResId).await()
        configuredDefaultsResId = defaultsResId
        configuredFetchIntervalSeconds = minimumFetchIntervalSeconds
    }

    override suspend fun fetchAndActivate(): Boolean {
        return firebaseRemoteConfig?.fetchAndActivate()?.await() ?: false
    }

    override suspend fun activate(): Boolean {
        return firebaseRemoteConfig?.activate()?.await() ?: false
    }

    override fun getString(key: String): String {
        return firebaseRemoteConfig?.getString(key).orEmpty()
    }

    override fun getValueSource(key: String): Int {
        return firebaseRemoteConfig?.getValue(key)?.source ?: FirebaseRemoteConfig.VALUE_SOURCE_STATIC
    }
}

@Singleton
class DefaultTenantAwareRemoteConfigResolver @Inject constructor() : TenantAwareRemoteConfigResolver {

    override fun buildTenantScopedKey(baseKey: String): String {
        return "tenant.${TenantRuntime.current.key}.$baseKey"
    }

    override fun resolve(
        baseKey: String,
        tenantAware: Boolean,
        hasKey: (String) -> Boolean
    ): String {
        if (!tenantAware) return baseKey
        val tenantKey = buildTenantScopedKey(baseKey)
        return if (hasKey(tenantKey)) tenantKey else baseKey
    }
}

@Singleton
class RemoteConfigFetchPolicy(
    val minimumFetchIntervalSeconds: Long
)

@Singleton
class FirebaseRemoteConfigService @Inject constructor(
    private val client: RemoteConfigClient,
    private val defaultsProvider: RemoteConfigDefaultsProvider,
    private val resolver: TenantAwareRemoteConfigResolver,
    private val fetchPolicy: RemoteConfigFetchPolicy
) : RemoteConfigService {

    override suspend fun fetchAndActivate(): Boolean {
        client.configure(
            defaultsResId = defaultsProvider.defaultsResId,
            minimumFetchIntervalSeconds = fetchPolicy.minimumFetchIntervalSeconds
        )
        return client.fetchAndActivate()
    }

    override suspend fun activate(): Boolean {
        client.configure(
            defaultsResId = defaultsProvider.defaultsResId,
            minimumFetchIntervalSeconds = fetchPolicy.minimumFetchIntervalSeconds
        )
        return client.activate()
    }

    override fun getResolvedKey(baseKey: String, tenantAware: Boolean): String {
        return resolver.resolve(baseKey = baseKey, tenantAware = tenantAware, hasKey = ::hasKey)
    }

    override fun getString(baseKey: String, tenantAware: Boolean): String {
        val resolvedKey = getResolvedKey(baseKey, tenantAware)
        val source = client.getValueSource(resolvedKey)
        return if (source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) {
            defaultsProvider.getString(resolvedKey).orEmpty()
        } else {
            client.getString(resolvedKey)
        }
    }

    override fun getBoolean(baseKey: String, tenantAware: Boolean): Boolean {
        return RemoteConfigKey.boolean(baseKey, tenantAware).mapper.decode(
            getString(baseKey, tenantAware)
        ) ?: false
    }

    override fun getLong(baseKey: String, tenantAware: Boolean): Long {
        return RemoteConfigKey.long(baseKey, tenantAware).mapper.decode(
            getString(baseKey, tenantAware)
        ) ?: 0L
    }

    override fun getDouble(baseKey: String, tenantAware: Boolean): Double {
        return RemoteConfigKey.double(baseKey, tenantAware).mapper.decode(
            getString(baseKey, tenantAware)
        ) ?: 0.0
    }

    override fun <T> getJson(
        baseKey: String,
        decoder: (String) -> T?,
        tenantAware: Boolean
    ): T? {
        return RemoteConfigKey.json(
            baseKey = baseKey,
            tenantAware = tenantAware,
            decoder = decoder
        ).mapper.decode(getString(baseKey, tenantAware))
    }

    override fun <T> getValue(key: RemoteConfigKey<T>): T? {
        return key.mapper.decode(getString(key.baseKey, key.tenantAware))
    }

    private fun hasKey(key: String): Boolean {
        if (defaultsProvider.contains(key)) return true
        return client.getValueSource(key) != FirebaseRemoteConfig.VALUE_SOURCE_STATIC
    }
}

@Singleton
class FirebaseRemoteConfigBootstrap @Inject constructor(
    private val remoteConfigService: RemoteConfigService,
    dispatcherProvider: DispatcherProvider
) : RemoteConfigBootstrap {

    private val scope = CoroutineScope(SupervisorJob() + dispatcherProvider.io)

    override fun preload() {
        scope.launch {
            runCatching { remoteConfigService.fetchAndActivate() }
        }
    }
}
