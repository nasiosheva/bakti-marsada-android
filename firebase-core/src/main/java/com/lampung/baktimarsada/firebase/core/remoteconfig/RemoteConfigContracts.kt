package com.lampung.baktimarsada.firebase.core.remoteconfig

fun interface RemoteConfigValueMapper<T> {
    fun decode(rawValue: String): T?
}

data class RemoteConfigKey<T>(
    val baseKey: String,
    val tenantAware: Boolean = true,
    val mapper: RemoteConfigValueMapper<T>
) {
    companion object {
        fun string(baseKey: String, tenantAware: Boolean = true): RemoteConfigKey<String> {
            return RemoteConfigKey(
                baseKey = baseKey,
                tenantAware = tenantAware,
                mapper = RemoteConfigValueMapper { it }
            )
        }

        fun boolean(baseKey: String, tenantAware: Boolean = true): RemoteConfigKey<Boolean> {
            return RemoteConfigKey(
                baseKey = baseKey,
                tenantAware = tenantAware,
                mapper = RemoteConfigValueMapper { raw ->
                    when (raw.trim().lowercase()) {
                        "true", "1", "yes", "on" -> true
                        "false", "0", "no", "off" -> false
                        else -> null
                    }
                }
            )
        }

        fun long(baseKey: String, tenantAware: Boolean = true): RemoteConfigKey<Long> {
            return RemoteConfigKey(
                baseKey = baseKey,
                tenantAware = tenantAware,
                mapper = RemoteConfigValueMapper { raw -> raw.trim().toLongOrNull() }
            )
        }

        fun double(baseKey: String, tenantAware: Boolean = true): RemoteConfigKey<Double> {
            return RemoteConfigKey(
                baseKey = baseKey,
                tenantAware = tenantAware,
                mapper = RemoteConfigValueMapper { raw -> raw.trim().toDoubleOrNull() }
            )
        }

        fun <T> json(
            baseKey: String,
            tenantAware: Boolean = true,
            decoder: (String) -> T?
        ): RemoteConfigKey<T> {
            return RemoteConfigKey(
                baseKey = baseKey,
                tenantAware = tenantAware,
                mapper = RemoteConfigValueMapper { raw -> decoder(raw) }
            )
        }
    }
}

interface RemoteConfigService {
    suspend fun fetchAndActivate(): Boolean
    suspend fun activate(): Boolean
    fun getResolvedKey(baseKey: String, tenantAware: Boolean = true): String
    fun getString(baseKey: String, tenantAware: Boolean = true): String
    fun getBoolean(baseKey: String, tenantAware: Boolean = true): Boolean
    fun getLong(baseKey: String, tenantAware: Boolean = true): Long
    fun getDouble(baseKey: String, tenantAware: Boolean = true): Double
    fun <T> getJson(
        baseKey: String,
        decoder: (String) -> T?,
        tenantAware: Boolean = true
    ): T?
    fun <T> getValue(key: RemoteConfigKey<T>): T?
}

interface TenantAwareRemoteConfigResolver {
    fun buildTenantScopedKey(baseKey: String): String
    fun resolve(
        baseKey: String,
        tenantAware: Boolean = true,
        hasKey: (String) -> Boolean
    ): String
}

interface RemoteConfigBootstrap {
    fun preload()
}
