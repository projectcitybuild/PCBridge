package com.projectcitybuild.plugin.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.plugin.events.FirstTimeJoinEvent
import com.projectcitybuild.repositories.PlayerConfigRepository
import com.projectcitybuild.support.spigot.eventbroadcast.LocalEventBroadcaster
import com.projectcitybuild.support.spigot.logger.PlatformLogger
import com.projectcitybuild.support.textcomponent.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import java.time.LocalDateTime
import javax.inject.Inject

class PlayerJoinListener @Inject constructor(
    private val server: Server,
    private val localEventBroadcaster: LocalEventBroadcaster,
    private val playerConfigRepository: PlayerConfigRepository,
    private val logger: PlatformLogger,
) : SpigotListener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        announceJoin(player = event.player)
        sendServerOverview(player = event.player)
        cachePlayer(player = event.player)
    }

    private fun announceJoin(player: Player) {
        server.broadcastMessage(
            TextComponent()
                .add("+ ") {
                    it.color = ChatColor.GREEN
                    it.isBold = true
                }
                .add(player.name) { it.color = ChatColor.WHITE }
                .add(" joined the server") { it.color = ChatColor.GRAY }
                .toLegacyText()
        )
    }

    private fun sendServerOverview(player: Player) {
        val onlinePlayerCount = server.onlinePlayers.size

        player.spigot().sendMessage(
            TextComponent().add(
                TextComponent.fromLegacyText(
                    """
                    #§3Welcome to §f§lPROJECT §6§lCITY §9§lBUILD
                    #
                    #§3Type §c/register §3to become a member.
                    #§3Type §c/list§6 §3to see who else is online.
                    #§3Players online:§c $onlinePlayerCount
                    #
                    #§f§lAsk our staff if you have any questions.
                """.trimMargin("#")
                )
            )
        )
    }

    private fun cachePlayer(player: Player) {
        val uuid = player.uniqueId

        if (playerConfigRepository.get(uuid) == null) {
            logger.debug("No player config found for $uuid. Generating new one...")

            localEventBroadcaster.emit(
                FirstTimeJoinEvent(player)
            )
            playerConfigRepository.add(
                uuid = uuid,
                isMuted = false,
                firstSeen = LocalDateTime.now(),
            )
        }
    }
}
