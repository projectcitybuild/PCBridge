package com.projectcitybuild.integrations.internal.playercache.listeners

import com.projectcitybuild.core.BungeecordListener
import com.projectcitybuild.features.joinmessage.events.FirstTimeJoinEvent
import com.projectcitybuild.integrations.internal.playercache.PlayerConfigCache
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.repositories.PlayerConfigRepository
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.event.EventHandler
import java.time.LocalDateTime
import javax.inject.Inject

class PlayerCacheListener @Inject constructor(
    private val localEventBroadcaster: LocalEventBroadcaster,
    private val playerCache: PlayerConfigCache,
    private val playerConfigRepository: PlayerConfigRepository,
    private val logger: PlatformLogger,
) : BungeecordListener {

    @EventHandler
    fun onPostLoginEvent(event: PostLoginEvent) {
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
    fun onPlayerDisconnectEvent(event: PlayerDisconnectEvent) {
        playerCache.remove(event.player.uniqueId)
    }
}
