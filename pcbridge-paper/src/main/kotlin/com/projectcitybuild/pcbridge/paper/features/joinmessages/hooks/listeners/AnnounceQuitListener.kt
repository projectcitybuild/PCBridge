package com.projectcitybuild.pcbridge.paper.features.joinmessages.hooks.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.libs.store.SessionStore
import com.projectcitybuild.pcbridge.paper.core.libs.store.Store
import com.projectcitybuild.pcbridge.paper.features.joinmessages.joinMessagesTracer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.round

class AnnounceQuitListener(
    private val remoteConfig: RemoteConfig,
    private val session: SessionStore,
    private val time: LocalizedTime,
) : Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerQuit(
        event: PlayerQuitEvent,
    ) = event.scopedSync(joinMessagesTracer, this::class.java) {
        val playerState = session.state.players[event.player.uniqueId]
        val sessionSeconds = playerState?.sessionSeconds(time) ?: 0
        val timeOnline = sessionTime(sessionSeconds)

        val leaveMessage = remoteConfig.latest.config.messages.leave

        event.quitMessage(
            MiniMessage.miniMessage().deserialize(
                leaveMessage,
                Placeholder.component("name", Component.text(event.player.name)),
                Placeholder.component("time_online", Component.text(timeOnline)),
            ),
        )
    }

    private fun sessionTime(seconds: Long): String {
        return when {
            seconds < 60 -> {
                "$seconds second" + if (seconds != 1L) "s" else ""
            }
            seconds < 3600 -> {
                val minutes = seconds / 60
                "$minutes minute" + if (minutes != 1L) "s" else ""
            }
            else -> {
                val hours = seconds / 3600
                "$hours hour" + if (hours != 1L) "s" else ""
            }
        }
    }
}
