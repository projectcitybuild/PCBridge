package com.projectcitybuild.modules.config.implementations

import com.projectcitybuild.modules.config.ConfigKeys
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
            ConfigKeys.SPIGOT_SERVER_NAME,
            ConfigKeys.DB_HOSTNAME,
            ConfigKeys.DB_PORT,
            ConfigKeys.DB_NAME,
            ConfigKeys.DB_USERNAME,
            ConfigKeys.DB_PASSWORD,
            ConfigKeys.REDIS_HOSTNAME,
            ConfigKeys.REDIS_PORT,
            ConfigKeys.REDIS_USERNAME,
            ConfigKeys.REDIS_PASSWORD,
            ConfigKeys.ERROR_REPORTING_SENTRY_ENABLED,
            ConfigKeys.ERROR_REPORTING_SENTRY_DSN,
            ConfigKeys.SHARED_CACHE_ADAPTER,
            ConfigKeys.SHARED_CACHE_FILE_RELATIVE_PATH,
            ConfigKeys.INTEGRATION_DYNMAP_WARP_ICON,
        ).forEach { key ->
            config.addDefault(key.key, key.defaultValue)
        }
        config.options().copyDefaults(true)
        plugin.saveConfig()
    }

    override fun <T> get(key: ConfigKeys.ConfigPath<T>): T {
        val value = config.get(key.key) as T
        if (value != null) {
            return value
        }
        return key.defaultValue
    }

    override fun get(path: String): Any? {
        return config.get(path)
    }

    override fun <T> set(key: ConfigKeys.ConfigPath<T>, value: T) {
        return config.set(key.key, value)
    }
}