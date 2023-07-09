package com.projectcitybuild.support.spigot

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class SpigotNamespace(
    private val plugin: JavaPlugin,
) {
    val invisibleKey get() = NamespacedKey(plugin, "invisible")
}