package com.projectcitybuild.pcbridge.paper.features.warps.events

import org.bukkit.event.HandlerList
import org.bukkit.event.Event as SpigotEvent

class WarpCreateEvent : SpigotEvent() {
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS
    }
}
