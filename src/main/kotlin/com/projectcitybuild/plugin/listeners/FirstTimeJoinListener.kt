package com.projectcitybuild.plugin.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.support.spigot.logger.PlatformLogger
import com.projectcitybuild.support.textcomponent.add
import com.projectcitybuild.plugin.events.FirstTimeJoinEvent
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.event.EventHandler
import javax.inject.Inject

class FirstTimeJoinListener @Inject constructor(
    private val server: Server,
    private val logger: PlatformLogger,
) : SpigotListener {

    @EventHandler
    fun onFirstTimeJoin(event: FirstTimeJoinEvent) {
        logger.debug("Sending first-time welcome message for ${event.player.name}")

        server.broadcastMessage(
            TextComponent()
                .add("✦ Welcome ") {
                    it.color = ChatColor.LIGHT_PURPLE
                }
                .add(event.player.name) {
                    it.color = ChatColor.WHITE
                }
                .add(" to the server!") {
                    it.color = ChatColor.LIGHT_PURPLE
                }
                .toLegacyText()
        )
    }
}
