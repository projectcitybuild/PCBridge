package com.projectcitybuild.pcbridge.features.playerstate.events

import com.projectcitybuild.pcbridge.http.responses.PlayerData
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

class PlayerStateCreatedEvent(
    val playerData: PlayerData,
    val playerUUID: UUID,
) : Event() {
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS
    }
}
