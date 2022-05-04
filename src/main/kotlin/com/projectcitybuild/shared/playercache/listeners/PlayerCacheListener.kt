package com.projectcitybuild.shared.playercache.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.features.joinmessage.events.FirstTimeJoinEvent
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.repositories.PlayerConfigRepository
import com.projectcitybuild.shared.playercache.PlayerConfigCache
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.time.LocalDateTime
import javax.inject.Inject

class PlayerCacheListener @Inject constructor(
    private val localEventBroadcaster: LocalEventBroadcaster,
    private val playerCache: PlayerConfigCache,
    private val playerConfigRepository: PlayerConfigRepository,
    private val logger: PlatformLogger,
) : SpigotListener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val uuid = event.player.uniqueId

        if (playerConfigRepository.get(uuid) == null) {
            logger.debug("No player config found for $uuid. Generating new one...")

            localEventBroadcaster.emit(
                FirstTimeJoinEvent(event.player)
            )
            playerConfigRepository.add(
                uuid = uuid,
                isMuted = false,
                isAllowingTPs = true,
                firstSeen = LocalDateTime.now(),
            )
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        playerCache.remove(event.player.uniqueId)
    }
}
