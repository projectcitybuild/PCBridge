package com.projectcitybuild.features.bans.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class BanCommand(
    private val proxyServer: ProxyServer,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val banRepository: BanRepository
): BungeecordCommand {

    override val label = "ban"
    override val permission = "pcbridge.ban.ban"
    override val usageHelp = "/ban <name> [reason]"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.isEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val staffPlayer = if (input.isConsoleSender) null else input.player
        val reason = input.args.joinWithWhitespaces(1 until input.args.size)
        val targetPlayerName = input.args.first()

        runCatching {
            val targetPlayerUUID = playerUUIDRepository.request(targetPlayerName)
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
                .firstOrNull { it.name.lowercase() == targetPlayerName.lowercase() || it.uniqueId == targetPlayerUUID }
                ?.disconnect(
                    TextComponent("You have been banned.\nAppeal @ projectcitybuild.com").also { it.color = ChatColor.RED }
                )

        }.onFailure { throwable ->
            input.sender.send().error(
                when (throwable) {
                    is BanRepository.PlayerAlreadyBannedException -> "$targetPlayerName is already banned"
                    else -> throwable.message ?: "An unknown error occurred"
                }
            )
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            args.size == 1 -> proxyServer.players.map { it.name }.filter { it.startsWith(args.first()) }
            else -> null
        }
    }
}