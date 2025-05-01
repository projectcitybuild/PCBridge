package com.projectcitybuild.pcbridge.paper.features.maintenance.listener

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MotdListener(
    private val store: Store,
) : Listener {
    @EventHandler
    fun onPing(event: PaperServerListPingEvent) {
        val state = store.state
        if (state.maintenance) {
            event.motd(
                MiniMessage.miniMessage().deserialize("<red>Server maintenance - be right back!</red>")
            )
        }
    }
}
