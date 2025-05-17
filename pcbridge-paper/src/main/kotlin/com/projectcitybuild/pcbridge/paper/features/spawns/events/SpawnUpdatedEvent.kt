package com.projectcitybuild.pcbridge.paper.features.spawns.events

import org.bukkit.Location
import org.bukkit.event.HandlerList
import java.util.UUID
import org.bukkit.event.Event as SpigotEvent

class SpawnUpdatedEvent(
    val worldId: UUID,
    val location: Location?
): SpigotEvent() {
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS
    }
}
