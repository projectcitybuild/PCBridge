package com.projectcitybuild.modules.eventbroadcast.implementations

import com.projectcitybuild.modules.eventbroadcast.BroadcastableEvent
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Event
import javax.inject.Inject

class BungeecordLocalEventBroadcaster @Inject constructor(
    private val proxyServer: ProxyServer,
) : LocalEventBroadcaster {

    override fun emit(event: BroadcastableEvent) {
        if (event !is Event) {
            throw Exception("Cannot cast event to Spigot Event [$event]")
        }
        proxyServer.pluginManager.callEvent(event)
    }
}
