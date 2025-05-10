package com.projectcitybuild.pcbridge.paper.features.tab.listeners

import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import com.projectcitybuild.pcbridge.paper.architecture.state.data.PlayerState
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.features.groups.RolesFilter
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
        return result.joinToString(separator = "<newline>")
    }
}