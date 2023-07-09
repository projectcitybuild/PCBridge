package com.projectcitybuild.support.spigot

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class SpigotNamespace(
    plugin: JavaPlugin,
) {
    val invisibleKey = NamespacedKey(plugin, "invisible")
}