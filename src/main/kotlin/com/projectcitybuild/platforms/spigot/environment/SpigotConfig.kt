package com.projectcitybuild.platforms.spigot.environment

import com.projectcitybuild.core.contracts.ConfigProvider
import com.projectcitybuild.core.entities.PluginConfig
import org.bukkit.configuration.file.FileConfiguration

class SpigotConfig(private val config: FileConfiguration): ConfigProvider {

    override fun <T> get(key: PluginConfig.Pair<T>): T {
        return config.get(key.key) as T
    }

    override fun <T> set(key: PluginConfig.Pair<T>, value: T) {
        return config.set(key.key, value)
    }
}