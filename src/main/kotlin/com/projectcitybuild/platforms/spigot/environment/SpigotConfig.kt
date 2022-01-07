package com.projectcitybuild.platforms.spigot.environment

import com.projectcitybuild.core.contracts.ConfigProvider
import com.projectcitybuild.entities.PluginConfig
import org.bukkit.configuration.file.FileConfiguration

class SpigotConfig(private val config: FileConfiguration): ConfigProvider {

    override fun <T> get(key: PluginConfig.ConfigPath<T>): T {
        return config.get(key.key) as T
    }

    override fun <T> set(key: PluginConfig.ConfigPath<T>, value: T) {
        return config.set(key.key, value)
    }

    override fun get(path: String): Any? {
        return config.get(path)
    }
}