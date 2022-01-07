package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.modules.bans.BanRepository
import com.projectcitybuild.modules.players.PlayerUUIDLookupService
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class BanCommand(
    private val proxyServer: ProxyServer,
    private val playerUUIDLookupService: PlayerUUIDLookupService,
    private val banRepository: BanRepository
): BungeecordCommand {

    override val label = "ban"
    override val permission = "pcbridge.ban.ban"
    override val usageHelp = "/ban <name> [reason]"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.isEmpty()) {
            input.sender.send().invalidCommandInput(this)
            return
        }

        val staffPlayer = if (input.isConsoleSender) null else input.player
        val reason = input.args.joinWithWhitespaces(1 until input.args.size)
        val targetPlayerName = input.args.first()

        runCatching {
            val targetPlayerUUID = playerUUIDLookupService.request(targetPlayerName)
                ?: throw Exception("Could not find UUID for $targetPlayerName. This player likely doesn't exist")

            banRepository.ban(
                targetPlayerUUID = targetPlayerUUID,
                targetPlayerName = targetPlayerName,
                staffId = staffPlayer?.uniqueId,
                reason = reason
            )

            proxyServer.broadcast(
                TextComponent("${ChatColor.GRAY}$targetPlayerName has been banned by ${input.sender.name}: ${reason?.isNotEmpty() ?: "No reason given"}")
            )

            proxyServer.players
                .first { it.name.lowercase() == targetPlayerName.lowercase() || it.uniqueId == targetPlayerUUID }
                ?.disconnect(
                    TextComponent("You have been banned.\nAppeal @ projectcitybuild.com").also { it.color = ChatColor.RED }
                )

        }.onFailure { throwable ->
            input.sender.send().error(
                if (throwable is BanRepository.PlayerAlreadyBannedException)
                    "$targetPlayerName is already banned"
                else
                    throwable.message ?: "An unknown error occurred"
            )
        }
    }
}