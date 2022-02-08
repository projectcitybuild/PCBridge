package com.projectcitybuild.modules.config.implementations

import com.projectcitybuild.entities.PluginConfig
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
            PluginConfig.SPIGOT_SERVER_NAME,
            PluginConfig.DB_HOSTNAME,
            PluginConfig.DB_PORT,
            PluginConfig.DB_NAME,
            PluginConfig.DB_USERNAME,
            PluginConfig.DB_PASSWORD,
            PluginConfig.REDIS_HOSTNAME,
            PluginConfig.REDIS_PORT,
            PluginConfig.REDIS_USERNAME,
            PluginConfig.REDIS_PASSWORD,
            PluginConfig.ERROR_REPORTING_SENTRY_ENABLED,
            PluginConfig.ERROR_REPORTING_SENTRY_DSN,
            PluginConfig.INTEGRATION_DYNMAP_WARP_ICON,
        ).forEach { key ->
            config.addDefault(key.key, key.defaultValue)
        }
        config.options().copyDefaults(true)
        plugin.saveConfig()
    }

    override fun <T> get(key: PluginConfig.ConfigPath<T>): T {
        val value = config.get(key.key) as T
        if (value != null) {
            return value
        }
        return key.defaultValue
    }

    override fun get(path: String): Any? {
        return config.get(path)
    }

    override fun <T> set(key: PluginConfig.ConfigPath<T>, value: T) {
        return config.set(key.key, value)
    }
}