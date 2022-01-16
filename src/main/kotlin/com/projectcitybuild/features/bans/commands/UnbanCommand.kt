package com.projectcitybuild.features.bans.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import javax.inject.Inject

class UnbanCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val banRepository: BanRepository
) : BungeecordCommand {

    override val label: String = "unban"
    override val permission: String = "pcbridge.ban.unban"
    override val usageHelp = "/unban <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val staffPlayer = if (input.isConsoleSender) null else input.player

        runCatching {
            val targetPlayerUUID = playerUUIDRepository.request(targetPlayerName)
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

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            args.size == 1 -> proxyServer.players.map { it.name }.filter { it.lowercase().startsWith(args.first()) }
            else -> null
        }
    }
}