package com.projectcitybuild.pcbridge.paper.features.joinmessages.hooks.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.features.joinmessages.joinMessagesTracer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ServerOverviewJoinListener(
    private val remoteConfig: RemoteConfig,
) : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onPlayerJoin(
        event: PlayerJoinEvent,
    ) = event.scopedSync(joinMessagesTracer, this::class.java) {
        val config = remoteConfig.latest.config
        val message = config.messages.welcome

        event.player.sendRichMessage(message)
    }
}
