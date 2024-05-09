package com.projectcitybuild.features.joinmessages.listeners

import com.projectcitybuild.core.config.Config
import com.projectcitybuild.features.joinmessages.repositories.PlayerConfigRepository
import com.projectcitybuild.core.datetime.time.Time
import com.projectcitybuild.support.PlatformLogger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class FirstTimeJoinListener(
    private val server: Server,
    private val logger: PlatformLogger,
    private val playerConfigRepository: PlayerConfigRepository,
    private val config: Config,
    private val time: Time,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun onPlayerJoin(event: PlayerJoinEvent) {
        val playerConfig = playerConfigRepository.get(event.player.uniqueId)
        if (playerConfig != null) {
            return
        }
        playerConfigRepository.add(
            uuid = event.player.uniqueId,
            firstSeen = time.now(),
        )

        logger.debug("Sending first-time welcome message for ${event.player.name}")

        val message = MiniMessage.miniMessage().deserialize(
            config.load().messages.firstTimeJoin,
            Placeholder.component("name", Component.text(event.player.name)),
        )
        server.onlinePlayers
            .filter { it.uniqueId != event.player.uniqueId }
            .forEach {it.sendMessage(message) }
    }
}
