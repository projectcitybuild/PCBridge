package com.projectcitybuild.pcbridge.features.bans.events

import com.projectcitybuild.pcbridge.http.models.IPBan
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class IPBanRequestedEvent(
    val ban: IPBan,
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
