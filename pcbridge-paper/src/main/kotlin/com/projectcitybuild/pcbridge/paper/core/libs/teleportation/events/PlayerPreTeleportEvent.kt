package com.projectcitybuild.pcbridge.paper.core.libs.teleportation.events

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Event as SpigotEvent

/**
 * Player is about to teleport (but hasn't yet)
 */
class PlayerPreTeleportEvent constructor(
    val player: Player,
) : SpigotEvent() {
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS
    }
}
