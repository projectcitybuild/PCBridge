package com.projectcitybuild.features.joinmessages.listeners

import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.entities.events.FirstTimeJoinEvent
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.core.modules.config.Config
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class FirstTimeJoinListener(
    private val server: Server,
    private val config: Config<PluginConfig>,
    private val logger: PlatformLogger,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onFirstTimeJoin(event: FirstTimeJoinEvent) {
        logger.debug("Sending first-time welcome message for ${event.player.name}")

        val message = MiniMessage.miniMessage().deserialize(
            config.get().messages.firstTimeJoin,
            Placeholder.component("name", Component.text(event.player.name)),
        )
        server.onlinePlayers
            .filter { it.uniqueId != event.player.uniqueId }
            .forEach {it.sendMessage(message) }
    }
}
