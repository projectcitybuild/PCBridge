package com.projectcitybuild.plugin.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.features.chat.ChatGroupFormatter
import com.projectcitybuild.modules.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.playercache.PlayerConfigCache
import com.projectcitybuild.plugin.events.FirstTimeJoinEvent
import com.projectcitybuild.repositories.PlayerConfigRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.time.LocalDateTime
import javax.inject.Inject

class PlayerCacheListener @Inject constructor(
    private val localEventBroadcaster: LocalEventBroadcaster,
    private val playerCache: PlayerConfigCache,
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatGroupFormatter: ChatGroupFormatter,
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
                firstSeen = LocalDateTime.now(),
            )
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        playerCache.remove(event.player.uniqueId)
        chatGroupFormatter.flush(event.player.uniqueId)
    }
}
