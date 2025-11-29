package com.projectcitybuild.pcbridge.paper.features.spawns.domain.data

import org.bukkit.Location
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

class SpawnUpdatedEvent(
    val worldId: UUID,
    val location: Location?
): Event() {
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS
    }
}