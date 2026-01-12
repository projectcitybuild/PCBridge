package com.projectcitybuild.pcbridge.paper.features.nicknames.listeners

import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.features.nicknames.NicknameTooLongException
import com.projectcitybuild.pcbridge.paper.features.nicknames.SetNickname
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.UUID

class NicknameListener(
    private val server: Server,
    private val setNickname: SetNickname,
): Listener {
    @EventHandler
    fun onPlayerStateCreated(event: PlayerStateCreatedEvent) {
        update(event.playerUUID, nickname = event.state.player?.nickname)
    }

    @EventHandler
    fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        if (event.prevState?.player?.nickname != event.state.player?.nickname) return

        update(event.playerUUID, nickname = event.state.player?.nickname)
    }

    private fun update(playerUUID: UUID, nickname: String?) {
        val player = server.getPlayer(playerUUID)
        if (player == null) {
            log.error { "Could not find player $playerUUID to update nickname" }
            return
        }
        try {
            setNickname.set(player, nickname)
        } catch (e: NicknameTooLongException) {
            setNickname.clear(player)
            log.warn { "Could not set nickname for $playerUUID: Nickname too long" }
        }
    }
}