package com.projectcitybuild.modules.pluginutils.listeners

import com.projectcitybuild.events.FirstTimeJoinEvent
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.repositories.PlayerConfigRepository
import com.projectcitybuild.support.spigot.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.support.spigot.listeners.SpigotListener
import org.bukkit.event.player.PlayerJoinEvent
import java.time.LocalDateTime

// TODO: move this to somewhere more appropriate
class CachePlayerOnJoinListener(
    private val localEventBroadcaster: LocalEventBroadcaster,
    private val playerConfigRepository: PlayerConfigRepository,
    private val logger: PlatformLogger,
) : SpigotListener<PlayerJoinEvent> {

    override suspend fun handle(event: PlayerJoinEvent) {
        val player = event.player
        val uuid = player.uniqueId

        if (playerConfigRepository.get(uuid) == null) {
            logger.debug("No player config found for $uuid. Generating new one...")

            localEventBroadcaster.emit(
                FirstTimeJoinEvent(player)
            )
            playerConfigRepository.add(
                uuid = uuid,
                isMuted = false,
                isChatBadgeDisabled = false,
                firstSeen = LocalDateTime.now(),
            )
        }
    }
}
