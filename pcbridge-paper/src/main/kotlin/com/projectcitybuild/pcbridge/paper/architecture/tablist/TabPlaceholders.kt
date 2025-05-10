package com.projectcitybuild.pcbridge.paper.architecture.tablist

import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotListenerRegistry
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.Listener

interface TabPlaceholder {
    val placeholder: String
    suspend fun value(player: Player): Component
}

/**
 * A [TabPlaceholder] which can trigger a tab re-render by an event
 */
interface UpdatableTabPlaceholder: TabPlaceholder, Listener

class TabPlaceholders(
    private val sectionPlaceholders: MutableSet<TabPlaceholder> = mutableSetOf(),
    private val playerPlaceholders: MutableSet<TabPlaceholder> = mutableSetOf(),
    private val listenerRegistry: SpigotListenerRegistry,
) {
    val player get() = playerPlaceholders.toSet()

    val section get() = sectionPlaceholders.toSet()

    fun section(placeholder: TabPlaceholder) {
        sectionPlaceholders.add(placeholder)

        if (placeholder is UpdatableTabPlaceholder) {
            listenerRegistry.register()
        }
    }

    fun player(placeholder: TabPlaceholder) {
        playerPlaceholders.add(placeholder)

        if (placeholder is UpdatableTabPlaceholder) {
            listenerRegistry.register()
        }
    }
}