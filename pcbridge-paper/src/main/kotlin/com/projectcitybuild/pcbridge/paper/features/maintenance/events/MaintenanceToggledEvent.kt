package com.projectcitybuild.pcbridge.paper.features.maintenance.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class MaintenanceToggledEvent(
    val enabled: Boolean,
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