package com.projectcitybuild.pcbridge.paper.features.motd.listeners

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MotdListener(
    private val remoteConfig: RemoteConfig,
) : Listener {
    @EventHandler
    fun onServerListPing(event: PaperServerListPingEvent) {
        val state = remoteConfig.latest.config
        val motd = state.motd
        if (motd.isNotEmpty()) {
            val message = MiniMessage.miniMessage().deserialize(motd)
            event.motd(message)
        } else {
            event.motd(Component.text("Project City Build"))
        }
    }
}
