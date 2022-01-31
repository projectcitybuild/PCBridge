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

    override fun <T> get(key: PluginConfig.ConfigPath<T>): T {
        val value = config.get(key.key) as T
        if (value != null) {
            return value
        }
        return key.defaultValue
    }

    override fun <T> set(key: PluginConfig.ConfigPath<T>, value: T) {
        return config.set(key.key, value)
    }

    override fun get(path: String): Any? {
        return config.get(path)
    }

    override fun load(vararg keys: PluginConfig.ConfigPath<*>) {
        keys.forEach { key ->
            config.addDefault(key.key, key.defaultValue)
        }
        config.options().copyDefaults(true)
        plugin.saveConfig()
    }
}