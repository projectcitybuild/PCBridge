package com.projectcitybuild.modules.config.implementations

import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import dagger.Reusable
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin

@Reusable
class SpigotConfig(
    private val plugin: Plugin,
    private val config: FileConfiguration
): PlatformConfig {

    init {
        generateDefaultConfig()
    }

    private fun generateDefaultConfig() {
        arrayOf(
            ConfigKey.SPIGOT_SERVER_NAME,
            ConfigKey.DB_HOSTNAME,
            ConfigKey.DB_PORT,
            ConfigKey.DB_NAME,
            ConfigKey.DB_USERNAME,
            ConfigKey.DB_PASSWORD,
            ConfigKey.REDIS_HOSTNAME,
            ConfigKey.REDIS_PORT,
            ConfigKey.REDIS_USERNAME,
            ConfigKey.REDIS_PASSWORD,
            ConfigKey.ERROR_REPORTING_SENTRY_ENABLED,
            ConfigKey.ERROR_REPORTING_SENTRY_DSN,
            ConfigKey.SHARED_CACHE_ADAPTER,
            ConfigKey.SHARED_CACHE_FILE_RELATIVE_PATH,
            ConfigKey.INTEGRATION_DYNMAP_WARP_ICON,
        ).forEach { key ->
            config.addDefault(key.key, key.defaultValue)
        }
        config.options().copyDefaults(true)
        plugin.saveConfig()
    }

    override fun <T> get(key: ConfigKey.ConfigPath<T>): T {
        val value = config.get(key.key) as T
        if (value != null) {
            return value
        }
        return key.defaultValue
    }

    override fun get(path: String): Any? {
        return config.get(path)
    }

    override fun <T> set(key: ConfigKey.ConfigPath<T>, value: T) {
        return config.set(key.key, value)
    }
}