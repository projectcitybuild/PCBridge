package com.projectcitybuild.pcbridge.features.onboarding.hooks.listeners

import com.projectcitybuild.pcbridge.features.onboarding.onboardingTracer
import com.projectcitybuild.pcbridge.runtime.listeners.tracedSync
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ServerOverviewJoinListener(
    private val remoteConfig: RemoteConfig,
) : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onPlayerJoin(event: PlayerJoinEvent) =
        event.tracedSync(onboardingTracer, this::class.java) {
            val config = remoteConfig.latest.config
            val message = config.messages.welcome

            event.player.sendRichMessage(message)
        }
}
