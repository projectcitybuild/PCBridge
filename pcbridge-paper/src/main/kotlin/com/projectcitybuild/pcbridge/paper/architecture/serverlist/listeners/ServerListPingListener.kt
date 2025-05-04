package com.projectcitybuild.pcbridge.paper.architecture.serverlist.listeners

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import com.projectcitybuild.pcbridge.paper.architecture.serverlist.decorators.ServerListing
import com.projectcitybuild.pcbridge.paper.architecture.serverlist.decorators.ServerListingDecoratorChain
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ServerListPingListener(
    private val decorators: ServerListingDecoratorChain,
) : Listener {
    @EventHandler
    suspend fun onServerListPing(event: PaperServerListPingEvent) {
        val serverListing = ServerListing()
        val decorated = decorators.pipe(serverListing)
        if (decorated.motd != null) {
            event.motd(decorated.motd)
        }
    }
}
