package com.projectcitybuild.modules.joinmessages.listeners

import com.projectcitybuild.entities.ConfigData
import com.projectcitybuild.entities.events.FirstTimeJoinEvent
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.support.spigot.SpigotServer
import com.projectcitybuild.support.spigot.listeners.SpigotListener
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler

class FirstTimeJoinListener(
    private val server: SpigotServer,
    private val config: Config<ConfigData>,
    private val logger: PlatformLogger,
) : SpigotListener<FirstTimeJoinEvent> {

    @EventHandler
    override suspend fun handle(event: FirstTimeJoinEvent) {
        logger.debug("Sending first-time welcome message for ${event.player.name}")

        val message = config.get().messages.firstTimeJoin
            .replace("%name%", event.player.name)

        server.broadcastMessage(
            TextComponent(message)
        )
    }
}
