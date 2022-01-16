package com.projectcitybuild.features.playercache.listeners

import com.projectcitybuild.core.BungeecordListener
import com.projectcitybuild.modules.playerconfig.PlayerConfigCache
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.event.EventHandler
import java.sql.Date

class PlayerCacheListener(
    private val playerCache: PlayerConfigCache,
    private val playerConfigRepository: PlayerConfigRepository,
): BungeecordListener {

    @EventHandler
    fun onPostLoginEvent(event: PostLoginEvent) {
        val uuid = event.player.uniqueId

        if (playerConfigRepository.get(uuid) == null) {
            playerConfigRepository.add(
                uuid = uuid,
                isMuted = false,
                isAllowingTPs = true,
                firstSeen = Date(System.currentTimeMillis()),
            )
        }
    }

    @EventHandler
    fun onPlayerDisconnectEvent(event: PlayerDisconnectEvent) {
        playerCache.remove(event.player.uniqueId)
    }
}