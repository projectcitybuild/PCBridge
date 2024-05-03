package com.projectcitybuild.features.joinmessages.listeners

import com.projectcitybuild.core.state.PlayerState
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
import org.bukkit.event.player.PlayerJoinEvent

class AnnounceJoinListener(
    private val config: Config<PluginConfig>,
    private val store: Store,
    private val time: Time,
): Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun onPlayerJoin(event: PlayerJoinEvent) {
        store.mutate { state ->
            val players = state.players
            players[event.player.uniqueId] = PlayerState(
                connectedAt = time.now(),
            )
            state.copy(
                players = players
            )
        }

        event.joinMessage(
            MiniMessage.miniMessage().deserialize(
                config.get().messages.join,
                Placeholder.component("name", Component.text(event.player.name)),
            )
        )
    }
}
