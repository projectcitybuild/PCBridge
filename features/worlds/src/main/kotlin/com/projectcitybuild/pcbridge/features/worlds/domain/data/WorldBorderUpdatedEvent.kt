package com.projectcitybuild.pcbridge.features.worlds.domain.data

import org.bukkit.World
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class WorldBorderUpdatedEvent(
    val world: World,
    val border: WorldBorder?,
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList

        private val handlerList = HandlerList()
    }
}
