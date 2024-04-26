package com.projectcitybuild.entities.events

import com.projectcitybuild.support.spigot.eventbroadcast.BroadcastableEvent
import org.bukkit.event.HandlerList
import org.bukkit.event.Event as SpigotEvent

class WarpCreateEvent : SpigotEvent(), BroadcastableEvent {

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
