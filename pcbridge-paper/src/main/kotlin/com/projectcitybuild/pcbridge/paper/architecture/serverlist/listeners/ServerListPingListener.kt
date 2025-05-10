package com.projectcitybuild.pcbridge.paper.architecture.serverlist.listeners

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import com.projectcitybuild.pcbridge.paper.architecture.serverlist.decorators.ServerListing
import com.projectcitybuild.pcbridge.paper.architecture.serverlist.decorators.ServerListingDecoratorChain
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ServerListPingListener(
    private val remoteConfig: RemoteConfig,
    private val decorators: ServerListingDecoratorChain,
) : Listener {
    @EventHandler
    suspend fun onServerListPing(event: PaperServerListPingEvent) {
        val decorated = decorators.pipe(ServerListing())
        event.motd(motd(decorated))
    }

    private fun motd(decorated: ServerListing): Component {
        if (decorated.motd != null) {
           return decorated.motd
        }
        val config = remoteConfig.latest.config
        val motd = config.motd
        if (motd.isEmpty()) {
            return Component.text("Project City Build")
        }
        return MiniMessage.miniMessage().deserialize(motd)
    }
}
