package com.projectcitybuild.pcbridge.paper.assembly.remoteconfig

import com.projectcitybuild.pcbridge.core.remoteconfig.RemoteConfigUpdatedEvent
import com.projectcitybuild.pcbridge.http.pcb.models.RemoteConfigVersion
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class SpigotRemoteConfigUpdatedEvent(
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

        fun fromEvent(event: RemoteConfigUpdatedEvent): SpigotRemoteConfigUpdatedEvent {
            return SpigotRemoteConfigUpdatedEvent(
                prev = event.prev,
                next = event.next,
            )
        }
    }
}