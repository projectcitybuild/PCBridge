package com.projectcitybuild.features.warps.events

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Event as SpigotEvent

/**
 * Player is about to warp (but hasn't yet)
 */
class PlayerPreWarpEvent constructor(
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
