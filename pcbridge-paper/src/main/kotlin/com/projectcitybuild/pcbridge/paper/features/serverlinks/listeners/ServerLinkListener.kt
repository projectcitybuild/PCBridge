package com.projectcitybuild.pcbridge.paper.features.serverlinks.listeners

import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import net.kyori.adventure.text.Component
import org.bukkit.ServerLinks
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLinksSendEvent
import java.net.URI

class ServerLinkListener(
    private val remoteConfig: RemoteConfig,
): Listener {
    @EventHandler
    fun oPlayerLinkSend(event: PlayerLinksSendEvent) {
        val config = remoteConfig.latest.config
        val serverLinks = config.serverLinks

        val links = event.links
        if (serverLinks.website.isNotEmpty()) {
            links.setLink(ServerLinks.Type.WEBSITE, URI(serverLinks.website))
        }
        serverLinks.custom.forEach { title, url ->
            links.addLink(Component.text(title), URI(url))
        }
    }
}