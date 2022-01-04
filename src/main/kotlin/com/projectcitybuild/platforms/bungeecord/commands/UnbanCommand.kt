package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.modules.bans.CreateUnbanAction
import com.projectcitybuild.modules.players.PlayerUUIDLookup
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.extensions.async
import com.projectcitybuild.platforms.spigot.send
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class UnbanCommand(
    private val proxyServer: ProxyServer,
    private val playerUUIDLookup: PlayerUUIDLookup,
    private val unbanAction: CreateUnbanAction
): BungeecordCommand {

    override val label: String = "unban"
    override val permission: String = "pcbridge.ban.unban"

    override fun execute(input: BungeecordCommandInput): CommandResult {
        if (!input.hasArguments)
            return CommandResult.INVALID_INPUT

        val targetPlayerName = input.args.first()
        val staffPlayer = if (input.isConsoleSender) null else input.player

        async {
            val targetPlayerUUID = playerUUIDLookup.request(targetPlayerName)
            if (targetPlayerUUID == null) {
                input.sender.send().error("Could not find UUID for $targetPlayerName. This player likely doesn't exist")
                return@async
            }

            val result = unbanAction.execute(
                playerId = targetPlayerUUID,
                staffId = staffPlayer?.uniqueId
            )
            when (result) {
                is Failure -> input.sender.send().error(
                    when (result.reason) {
                        is CreateUnbanAction.FailReason.PlayerNotBanned -> "$targetPlayerName is not currently banned"
                        is CreateUnbanAction.FailReason.HTTPError -> "Bad request sent to the ban server. Please contact an admin"
                        is CreateUnbanAction.FailReason.NetworkError -> "Failed to contact auth server. Please contact an admin"
                    }
                )
                is Success -> proxyServer.broadcast(
                    TextComponent("${input.args.first()} has been unbanned").also { it.color = ChatColor.GRAY }
                )
            }
        }

        return CommandResult.EXECUTED
    }
}