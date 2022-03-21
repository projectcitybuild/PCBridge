package com.projectcitybuild.features.warps.events

import com.projectcitybuild.modules.eventbroadcast.BroadcastableEvent
import org.bukkit.event.HandlerList
import org.bukkit.event.Event as SpigotEvent

class WarpDeleteEvent : SpigotEvent(), BroadcastableEvent {

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }
}
