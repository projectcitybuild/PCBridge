package com.projectcitybuild.features.bans.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.usecases.CheckBanUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import javax.inject.Inject

class CheckBanCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val checkBanUseCase: CheckBanUseCase,
) : BungeecordCommand {

    override val label = "checkban"
    override val permission = "pcbridge.ban.checkban"
    override val usageHelp = "/checkban <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val result = checkBanUseCase.getBan(targetPlayerName)

        when (result) {
            is Failure -> {
                if (result.reason == CheckBanUseCase.FailureReason.PlayerDoesNotExist) {
                    input.sender.send().error("Could not find UUID for $targetPlayerName. This player likely doesn't exist")
                }
            }
            is Success -> {
                val ban = result.value
                if (ban == null) {
                    input.sender.send().info("$targetPlayerName is not currently banned")
                } else {
                    input.sender.send().info(
                        """
                            #${ChatColor.RED}$targetPlayerName is currently banned.
                            #${ChatColor.GRAY}---
                            #${ChatColor.GRAY}Reason » ${ChatColor.WHITE}${ban.reason}
                            #${ChatColor.GRAY}Date » ${ChatColor.WHITE}${ban.dateOfBan}
                            #${ChatColor.GRAY}Expires » ${ChatColor.WHITE}${ban.expiryDate}
                        """.trimMargin("#"),
                        isMultiLine = true
                    )
                }
            }
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            args.size == 1 -> proxyServer.players.map { it.name }.filter { it.lowercase().startsWith(args.first().lowercase()) }
            else -> null
        }
    }
}