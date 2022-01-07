package com.projectcitybuild.platforms.spigot.extensions

import com.projectcitybuild.entities.PluginConfig
import org.bukkit.configuration.file.FileConfiguration

fun <T> FileConfiguration.addDefault(pair: PluginConfig.ConfigPath<T>) {
    addDefault(pair.key, pair.defaultValue)
}
