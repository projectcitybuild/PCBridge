package com.projectcitybuild.platforms.spigot.extensions

import com.projectcitybuild.entities.PluginConfig
import org.bukkit.configuration.file.FileConfiguration

inline fun <T> FileConfiguration.addDefault(pair: PluginConfig.Pair<T>) {
    addDefault(pair.key, pair.defaultValue)
}
