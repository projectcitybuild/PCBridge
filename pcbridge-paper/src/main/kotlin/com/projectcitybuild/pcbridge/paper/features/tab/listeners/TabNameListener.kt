package com.projectcitybuild.pcbridge.paper.features.tab.listeners

import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import com.projectcitybuild.pcbridge.paper.architecture.state.data.PlayerState
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

class TabNameListener(
    private val server: Server,
    private val store: Store,
) : Listener {
    @EventHandler
    fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        log.info { "test" }

        updatePlayerName(
            uuid = event.playerUUID,
            playerState = event.state,
        )
        updateTabListForEveryone()
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val uuid = event.player.uniqueId

        // [PlayerStateUpdatedEvent] is fired before the player joins when their
        // state is initially created. For just this case we need to watch for player
        // join events
        val playerState = store.state.players[uuid]
        if (playerState != null) {
            updatePlayerName(uuid, playerState)
        }

        updateTabListForEveryone()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        updateTabListForEveryone()
    }

    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        updateTabListForPlayer(event.player)
    }

    private fun updatePlayerName(uuid: UUID, playerState: PlayerState) {
        log.info { "Updating tab name for player $uuid" }
        val player = server.getPlayer(uuid) ?: return
        var name = player.displayName()

        if (playerState.afk) {
            name = Component.text("[AFK]").append(name)
        }
        log.info { "AFK: ${playerState.afk}" }
        log.info { "Name: $name" }
        player.playerListName(name)
    }

    private fun updateTabListForEveryone() {
        server.onlinePlayers.forEach(::updateTabListForPlayer)
    }

    private fun updateTabListForPlayer(player: Player) {
        val miniMessage = MiniMessage.miniMessage()
        val header = miniMessage.deserialize(
            header(
                TabHeader(
                    worldName = player.location.world.name,
                    onlinePlayers = server.onlinePlayers.size,
                    maxPlayers = server.maxPlayers,
                )
            )
        )
        val footer = miniMessage.deserialize(footer(player))

        player.sendPlayerListHeaderAndFooter(header, footer)
    }

    private fun header(header: TabHeader): String {
        return listOf(
            "<gradient:dark_aqua:green>────────────>>> <bold><white>Project</white> <yellow>City</yellow> <blue>Build</blue></bold> <<<────────────</gradient>",
            "<gray>projectcitybuild.com</gray>",
            "<gray>World:</gray> <aqua>${header.worldName}</aqua>",
            "<gray>Online:</gray> <aqua>${header.onlinePlayers}/${header.maxPlayers}</aqua>",
            "",
        ).joinToString(separator = "<newline>")
    }

    private fun footer(player: Player): String {
        return listOf(
            "",
            "<gray>Rank up to Member with <red><bold>/register</bold></red></gray>",
            "<gradient:dark_aqua:green>───────────────────────────────────────</gradient>",
        ).joinToString(separator = "<newline>")
    }
}

data class TabHeader(
    val worldName: String,
    val onlinePlayers: Int,
    val maxPlayers: Int,
)