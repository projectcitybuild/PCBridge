package com.projectcitybuild.pcbridge.paper.core.support.spigot

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class SpigotNamespace(
    private val plugin: JavaPlugin,
) {
    abstract class Key(val identifier: String)

    private val keys: MutableMap<String, NamespacedKey> = mutableMapOf()

    fun get(key: Key): NamespacedKey {
        val namespaced = keys[key.identifier]
        if (namespaced != null) {
            return namespaced
        }
        val newKey = NamespacedKey(plugin, key.identifier)
        keys[key.identifier] = newKey
        return newKey
    }
}
