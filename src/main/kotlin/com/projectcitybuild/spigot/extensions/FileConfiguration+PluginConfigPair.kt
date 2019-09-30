package com.projectcitybuild.spigot.extensions

import com.projectcitybuild.entities.PluginConfigPair
import org.bukkit.configuration.file.FileConfiguration

inline fun <reified Pair: PluginConfigPair> FileConfiguration.addDefault() {
    val pair = Pair::class.java.newInstance()
    addDefault(pair.key, pair.defaultValue)
}
