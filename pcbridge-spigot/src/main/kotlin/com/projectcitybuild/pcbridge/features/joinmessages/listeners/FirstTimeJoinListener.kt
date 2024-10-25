package com.projectcitybuild.pcbridge.features.joinmessages.listeners

import com.projectcitybuild.pcbridge.core.datetime.LocalizedTime
import com.projectcitybuild.pcbridge.core.logger.log
import com.projectcitybuild.pcbridge.core.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.features.joinmessages.repositories.PlayerConfigRepository
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
    private val playerConfigRepository: PlayerConfigRepository,
    private val remoteConfig: RemoteConfig,
    private val time: LocalizedTime,
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

        log.debug { "Sending first-time welcome message for ${event.player.name}" }

        val config = remoteConfig.latest.config
        val message =
            MiniMessage.miniMessage().deserialize(
                config.messages.firstTimeJoin,
                Placeholder.component("name", Component.text(event.player.name)),
            )
        server.onlinePlayers
            .filter { it.uniqueId != event.player.uniqueId }
            .forEach { it.sendMessage(message) }
    }
}
