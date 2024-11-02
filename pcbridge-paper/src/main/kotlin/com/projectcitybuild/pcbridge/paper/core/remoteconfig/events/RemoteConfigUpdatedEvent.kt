package com.projectcitybuild.pcbridge.paper.core.remoteconfig.events

import com.projectcitybuild.pcbridge.http.models.pcb.RemoteConfigVersion
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class RemoteConfigUpdatedEvent(
    val prev: RemoteConfigVersion?,
    val next: RemoteConfigVersion,
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
