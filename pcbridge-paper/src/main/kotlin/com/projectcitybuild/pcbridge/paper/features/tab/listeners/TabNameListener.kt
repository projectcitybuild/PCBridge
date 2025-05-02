package com.projectcitybuild.pcbridge.paper.features.tab.listeners

import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import com.projectcitybuild.pcbridge.paper.architecture.state.data.PlayerState
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.roles.RolesFilter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
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
    private val rolesFilter: RolesFilter,
) : Listener {
    @EventHandler
    fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
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
        val player = server.getPlayer(uuid) ?: return
        var name = player.displayName()

        if (playerState.afk) {
            name = Component.text("⌚ ")
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.BOLD)
                .append(name.decorate(TextDecoration.ITALIC))
        }
        player.playerListName(name)
    }

    private fun updateTabListForEveryone() {
        server.onlinePlayers.forEach(::updateTabListForPlayer)
    }

    private fun updateTabListForPlayer(player: Player) {
        val playerState = store.state.players[player.uniqueId]

        val roles = rolesFilter.filter(playerState?.groups?.toSet() ?: emptySet())

        val miniMessage = MiniMessage.miniMessage()
        val header = miniMessage.deserialize(
            header(
                TabHeader(
                    worldName = player.location.world.name,
                    onlinePlayers = server.onlinePlayers.size,
                    maxPlayers = server.maxPlayers,
                    roles = roles.values.mapNotNull { it.minecraftName },
                )
            )
        )
        val footer = miniMessage.deserialize(
            footer(playerState)
        )

        player.sendPlayerListHeaderAndFooter(header, footer)
    }

    private fun header(header: TabHeader): String {
        val groups = if (header.roles == null) "Unknown"
            else if (header.roles.isEmpty()) "Guest"
            else header.roles.joinToString(separator = ", ")

        return listOf(
            "<gradient:dark_aqua:green>────────────>>> <bold><white>Project</white> <gold>City</gold> <blue>Build</blue></bold> <<<────────────</gradient>",
            "<gray>World:</gray> <aqua>${header.worldName}</aqua>",
            "<gray>Online:</gray> <aqua>${header.onlinePlayers}/${header.maxPlayers}</aqua>",
            groups,
            "",
        ).joinToString(separator = "<newline>")
    }

    private fun footer(playerState: PlayerState?): String {
        val result = mutableListOf(
            "",
            "<gray>Join us on <bold>/discord</bold></gray>",
            "<gray>projectcitybuild.com</gray>",
            "<gradient:dark_aqua:green>────────────────────────────────────────</gradient>",
        )
        if (playerState == null || playerState.groups.isEmpty()) {
            result.add(1, "<gray>Rank up to Member with </gray><bold><red>/register</red></bold>")
        }
        return result.joinToString(separator = "<newline>")
    }
}

data class TabHeader(
    val worldName: String,
    val onlinePlayers: Int,
    val maxPlayers: Int,
    val roles: List<String>?,
)