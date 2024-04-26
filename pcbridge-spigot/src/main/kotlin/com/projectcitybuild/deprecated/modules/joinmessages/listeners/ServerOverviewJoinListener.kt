package com.projectcitybuild.modules.joinmessages.listeners

import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.support.spigot.listeners.SpigotListener
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent

class ServerOverviewJoinListener(
    private val config: Config<PluginConfig>,
) : SpigotListener<PlayerJoinEvent> {

    @EventHandler
    override suspend fun handle(event: PlayerJoinEvent) {
        val message = config.get().messages.welcome

        event.player.spigot().sendMessage(
            TextComponent(message)
        )
    }
}
