package com.projectcitybuild.shared.crossteleport.events

import com.projectcitybuild.modules.eventbroadcast.BroadcastableEvent
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Event as SpigotEvent

class PlayerPreLocationTeleportEvent(
    val player: Player,
    val currentLocation: Location,
) : SpigotEvent(), BroadcastableEvent {

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
