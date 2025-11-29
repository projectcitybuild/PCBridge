package com.projectcitybuild.pcbridge.paper.features.joinmessages.hooks.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.features.joinmessages.joinMessagesTracer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class AnnounceJoinListener(
    private val remoteConfig: RemoteConfig,
) : Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerJoin(
        event: PlayerJoinEvent,
    ) = event.scopedSync(joinMessagesTracer, this::class.java) {
        val joinMessage = remoteConfig.latest.config.messages.join

        event.joinMessage(
            MiniMessage.miniMessage().deserialize(
                joinMessage,
                Placeholder.component("name", Component.text(event.player.name)),
            ),
        )
    }
}
