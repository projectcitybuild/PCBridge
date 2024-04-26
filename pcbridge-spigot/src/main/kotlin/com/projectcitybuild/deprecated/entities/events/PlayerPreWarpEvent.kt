package com.projectcitybuild.entities.events

import com.projectcitybuild.support.spigot.eventbroadcast.BroadcastableEvent
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Event as SpigotEvent

/**
 * Player is about to warp (but hasn't yet)
 */
class PlayerPreWarpEvent constructor(
    val player: Player,
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
