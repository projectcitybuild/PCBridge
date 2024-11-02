package com.projectcitybuild.pcbridge.paper.features.watchdog.listeners.events

import net.kyori.adventure.text.Component
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ItemRenamedEvent(
    val displayName: Component,
) : Event() {
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS
    }
}
