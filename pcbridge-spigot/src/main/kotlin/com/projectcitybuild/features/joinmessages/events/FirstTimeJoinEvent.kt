package com.projectcitybuild.features.joinmessages.events

import com.projectcitybuild.support.spigot.eventbroadcast.BroadcastableEvent
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class FirstTimeJoinEvent(val player: Player) : Event(), BroadcastableEvent {

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
