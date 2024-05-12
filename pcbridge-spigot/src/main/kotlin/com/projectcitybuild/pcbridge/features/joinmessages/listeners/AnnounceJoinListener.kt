package com.projectcitybuild.pcbridge.features.joinmessages.listeners

import com.projectcitybuild.pcbridge.core.config.Config
import com.projectcitybuild.pcbridge.core.datetime.LocalizedTime
import com.projectcitybuild.pcbridge.core.state.PlayerState
import com.projectcitybuild.pcbridge.core.state.Store
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class AnnounceJoinListener(
    private val config: Config,
    private val store: Store,
    private val time: LocalizedTime,
) : Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun onPlayerJoin(event: PlayerJoinEvent) {
        store.mutate { state ->
            val players =
                state.players.also {
                    it[event.player.uniqueId] =
                        it
                            .getOrDefault(event.player.uniqueId, defaultValue = PlayerState.empty())
                            .copy(connectedAt = time.now())
                }
            state.copy(
                players = players,
            )
        }

        event.joinMessage(
            MiniMessage.miniMessage().deserialize(
                config.get().messages.join,
                Placeholder.component("name", Component.text(event.player.name)),
            ),
        )
    }
}
