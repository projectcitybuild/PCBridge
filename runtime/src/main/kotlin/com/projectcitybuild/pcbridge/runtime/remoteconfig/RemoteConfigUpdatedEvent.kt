package com.projectcitybuild.pcbridge.runtime.remoteconfig

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
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
