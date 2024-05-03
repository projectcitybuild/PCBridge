package com.projectcitybuild.features.joinmessages.listeners

import com.projectcitybuild.core.state.Store
import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.pcbridge.core.modules.datetime.time.Time
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
    private val config: Config<PluginConfig>,
    private val store: Store,
    private val time: Time,
): Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun onPlayerQuit(event: PlayerQuitEvent) {
        val playerState = store.state.players[event.player.uniqueId]
        val joinTime = playerState?.connectedAt ?: time.now()

        store.mutate { state ->
            state.copy(
                players = state.players.apply { remove(event.player.uniqueId) },
            )
        }

        val timeOnline = sessionTime(start = joinTime)

        event.quitMessage(
            MiniMessage.miniMessage().deserialize(
                config.get().messages.leave,
                Placeholder.component("name", Component.text(event.player.name)),
                Placeholder.component("time_online", Component.text(timeOnline)),
            )
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
        } else if(minsOnline < 60) {
            "$minsOnline min" + if (minsOnline > 1) "s" else ""
        } else {
            "$hoursOnline hour" + if (hoursOnline > 1) "s" else ""
        }
    }
}
