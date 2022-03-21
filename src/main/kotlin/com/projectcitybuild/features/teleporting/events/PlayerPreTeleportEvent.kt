package com.projectcitybuild.features.teleporting.events

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Event as SpigotEvent

class PlayerPreTeleportEvent(
    val player: Player,
    val currentLocation: Location,
) : SpigotEvent() {

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
