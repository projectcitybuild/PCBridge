package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.modules.bans.BanRepository
import com.projectcitybuild.modules.players.PlayerUUIDLookup
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class UnbanCommand(
    private val proxyServer: ProxyServer,
    private val playerUUIDLookup: PlayerUUIDLookup,
    private val banRepository: BanRepository
) : BungeecordCommand {

    override val label: String = "unban"
    override val permission: String = "pcbridge.ban.unban"

    override fun validate(input: BungeecordCommandInput) : CommandResult {
        if (input.args.isEmpty())
            return CommandResult.INVALID_INPUT

        return CommandResult.EXECUTED
    }

    override suspend fun execute(input: BungeecordCommandInput) {
        val targetPlayerName = input.args.first()
        val staffPlayer = if (input.isConsoleSender) null else input.player

        runCatching {
            val targetPlayerUUID = playerUUIDLookup.request(targetPlayerName)
                ?: throw Exception("Could not find UUID for $targetPlayerName. This player likely doesn't exist")

            banRepository.unban(
                targetPlayerUUID = targetPlayerUUID,
                staffId = staffPlayer?.uniqueId
            )

        }.onFailure { throwable ->
            input.sender.send().error(
                if (throwable is BanRepository.PlayerNotBannedException)
                    "$targetPlayerName is not currently banned"
                else
                    throwable.message ?: "An unknown error occurred"
            )
            return
        }

        proxyServer.broadcast(
            TextComponent("${input.args.first()} has been unbanned").also { it.color = ChatColor.GRAY }
        )
    }
}