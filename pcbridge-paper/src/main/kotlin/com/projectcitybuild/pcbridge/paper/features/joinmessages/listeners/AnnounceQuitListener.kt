package com.projectcitybuild.pcbridge.paper.features.joinmessages.listeners

import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.architecture.store.Store
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.time.Duration
import java.time.LocalDateTime

class AnnounceQuitListener(
    private val remoteConfig: RemoteConfig,
    private val store: Store,
    private val time: LocalizedTime,
) : Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val playerState = store.state.players[event.player.uniqueId]
        val joinTime = playerState?.connectedAt ?: time.now()
        val timeOnline = sessionTime(start = joinTime)

        val leaveMessage = remoteConfig.latest.config.messages.leave

        event.quitMessage(
            MiniMessage.miniMessage().deserialize(
                leaveMessage,
                Placeholder.component("name", Component.text(event.player.name)),
                Placeholder.component("time_online", Component.text(timeOnline)),
            ),
        )
    }

    private fun sessionTime(start: LocalDateTime): String {
        val now = time.now()
        val diff = Duration.between(start, now)

        val secsOnline = diff.toSeconds()
        val minsOnline = diff.toMinutes()
        val hoursOnline = diff.toHours()

        return if (secsOnline < 60) {
            "$secsOnline sec" + if (secsOnline > 1) "s" else ""
        } else if (minsOnline < 60) {
            "$minsOnline min" + if (minsOnline > 1) "s" else ""
        } else {
            "$hoursOnline hour" + if (hoursOnline > 1) "s" else ""
        }
    }
}
