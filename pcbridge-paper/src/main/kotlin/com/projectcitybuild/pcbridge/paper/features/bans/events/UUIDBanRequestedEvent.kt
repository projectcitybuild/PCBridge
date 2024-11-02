package com.projectcitybuild.pcbridge.paper.features.bans.events

import com.projectcitybuild.pcbridge.http.models.pcb.PlayerBan
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class UUIDBanRequestedEvent(
    val ban: PlayerBan,
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
