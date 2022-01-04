package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.modules.bans.CreateBanAction
import com.projectcitybuild.modules.players.PlayerUUIDLookup
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.extensions.async
import com.projectcitybuild.platforms.spigot.send
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class BanCommand(
    private val proxyServer: ProxyServer,
    private val playerUUIDLookup: PlayerUUIDLookup,
    private val createBanAction: CreateBanAction
): BungeecordCommand {

    override val label = "ban"
    override val permission = "pcbridge.ban.ban"

    override fun execute(input: BungeecordCommandInput): CommandResult {
        if (!input.hasArguments)
            return CommandResult.INVALID_INPUT

        val staffPlayer = if (input.isConsoleSender) null else input.player
        val reason = input.args.joinWithWhitespaces(1 until input.args.size)
        val targetPlayerName = input.args.first()

        async {
            val targetPlayerUUID = playerUUIDLookup.request(targetPlayerName)
            if (targetPlayerUUID == null) {
                input.sender.send().error("Could not find UUID for $targetPlayerName. This player likely doesn't exist")
                return@async
            }

            val result = createBanAction.execute(
                playerId = targetPlayerUUID,
                playerName = targetPlayerName,
                staffId = staffPlayer?.uniqueId,
                reason = reason
            )
            when (result) {
                is Failure -> input.sender.send().error(
                    when (result.reason) {
                        is CreateBanAction.FailReason.HTTPError -> "Bad response received from the ban server. Please contact an admin"
                        is CreateBanAction.FailReason.NetworkError -> "Failed to contact auth server. Please contact an admin"
                        is CreateBanAction.FailReason.PlayerAlreadyBanned -> "$targetPlayerName is already banned"
                    }
                )
                is Success -> {
                    proxyServer.broadcast(
                        TextComponent("${ChatColor.GRAY}${input.args.first()} has been banned by ${input.sender.name}: ${reason?.isNotEmpty() ?: "No reason given"}")
                    )
                    proxyServer.players
                        .first { it.name.lowercase() == targetPlayerName.lowercase() }
                        ?.disconnect(
                            TextComponent("You have been banned").also { it.color = ChatColor.RED }
                        )
                }
            }
        }

        return CommandResult.EXECUTED
    }
}