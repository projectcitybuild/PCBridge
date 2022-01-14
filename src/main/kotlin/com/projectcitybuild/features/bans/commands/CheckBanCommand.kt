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
import java.text.SimpleDateFormat
import java.util.*

class CheckBanCommand(
    private val proxyServer: ProxyServer,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val banRepository: BanRepository
) : BungeecordCommand {

    override val label = "checkban"
    override val permission = "pcbridge.ban.checkban"
    override val usageHelp = "/checkban <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()

        val result = runCatching {
            val targetPlayerUUID = playerUUIDRepository.request(targetPlayerName)
                ?: throw Exception("Could not find UUID for $targetPlayerName. This player likely doesn't exist")

            banRepository.get(targetPlayerUUID = targetPlayerUUID)

        }.onFailure { throwable ->
            input.sender.send().error(
                if (throwable is BanRepository.PlayerAlreadyBannedException)
                    "$targetPlayerName is already banned"
                else
                    throwable.message ?: "An unknown error occurred"
            )
            return
        }

        val ban = result.getOrNull()
        if (ban == null) {
            input.sender.send().info("$targetPlayerName is not currently banned")
        } else {
            val banDate = ban.createdAt.let {
                val date = Date(it * 1000)
                val format = SimpleDateFormat("yyyy/MM/dd HH:mm")
                format.format(date)
            }
            val expiryDate = ban.expiresAt?.let {
                val date = Date(it * 1000)
                val format = SimpleDateFormat("yyyy/MM/dd HH:mm")
                format.format(date)
            } ?: "Never"

            input.sender.send().info(
                """
                            #${ChatColor.RED}$targetPlayerName is currently banned.
                            #${ChatColor.GRAY}---
                            #${ChatColor.GRAY}Reason » ${ChatColor.WHITE}${ban.reason}
                            #${ChatColor.GRAY}Date » ${ChatColor.WHITE}$banDate
                            #${ChatColor.GRAY}Expires » ${ChatColor.WHITE}$expiryDate
                        """.trimMargin("#"),
                isMultiLine = true
            )
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            args.size == 1 -> proxyServer.players.map { it.name }.filter { it.lowercase().startsWith(args.first()) }
            else -> null
        }
    }
}