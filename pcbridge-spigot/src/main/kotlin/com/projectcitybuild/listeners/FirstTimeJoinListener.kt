package com.projectcitybuild.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.events.FirstTimeJoinEvent
import com.projectcitybuild.support.textcomponent.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.event.EventHandler

class FirstTimeJoinListener(
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
