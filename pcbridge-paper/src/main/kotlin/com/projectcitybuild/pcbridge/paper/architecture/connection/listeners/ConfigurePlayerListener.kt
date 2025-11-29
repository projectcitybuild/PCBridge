package com.projectcitybuild.pcbridge.paper.architecture.connection.listeners

import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ConfigurePlayerListener: Listener {
    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true,
    )
    fun onAsyncPlayerConnectionConfigureEvent(event: AsyncPlayerConnectionConfigureEvent) {
        print("test")
    }
}