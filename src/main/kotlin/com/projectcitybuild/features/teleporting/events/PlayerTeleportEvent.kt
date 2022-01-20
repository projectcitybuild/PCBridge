package com.projectcitybuild.features.teleporting.events

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Event as SpigotEvent

class PlayerTeleportEvent(
    val player: Player,
    val destinationPlayer: Player,
): SpigotEvent() {

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    val destination: Location
        get() = destinationPlayer.location

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }
}
