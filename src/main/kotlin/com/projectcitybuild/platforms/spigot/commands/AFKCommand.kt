package com.projectcitybuild.platforms.spigot.commands

import com.google.common.io.ByteStreams
import com.projectcitybuild.core.contracts.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.CommandInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.platforms.spigot.SessionCache
import com.projectcitybuild.platforms.spigot.environment.send
import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class AFKCommand(
    private val plugin: Plugin,
    private val sessionCache: SessionCache
): Commandable {

    override val label: String = "afk"
    override val permission: String = "pcbridge.afk"

    override suspend fun execute(input: CommandInput): CommandResult {
        if (input.args.isNotEmpty())
            return CommandResult.INVALID_INPUT

        if (input.isConsole) {
            input.sender.send().error("Console cannot use this command")
            return CommandResult.EXECUTED
        }

        val player = input.sender as Player
        if (sessionCache.afkPlayerList.contains(player.uniqueId)) {
            sessionCache.afkPlayerList.add(player.uniqueId)
            plugin.server.broadcastMessage("${ChatColor.GRAY}${player.displayName} is now AFK")
        } else {
            sessionCache.afkPlayerList.remove(player.uniqueId)
            plugin.server.broadcastMessage("${ChatColor.GRAY}${player.displayName} is no longer AFK")
        }

        return CommandResult.EXECUTED
    }
}
