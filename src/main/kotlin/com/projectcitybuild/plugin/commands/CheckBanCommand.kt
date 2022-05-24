package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.usecases.CheckUUIDBanUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import com.projectcitybuild.plugin.exceptions.InvalidCommandArgumentsException
import net.md_5.bungee.api.ChatColor
import org.bukkit.Server
import org.bukkit.command.CommandSender
import javax.inject.Inject

class CheckBanCommand @Inject constructor(
    private val server: Server,
    private val checkUUIDBanUseCase: CheckUUIDBanUseCase,
) : SpigotCommand {

    override val label = "checkban"
    override val permission = "pcbridge.ban.checkban"
    override val usageHelp = "/checkban <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val result = checkUUIDBanUseCase.getBan(targetPlayerName)

        when (result) {
            is Failure -> {
                if (result.reason == CheckUUIDBanUseCase.FailureReason.PLAYER_DOES_NOT_EXIST) {
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
            args.isEmpty() -> server.onlinePlayers.map { it.name }
            args.size == 1 -> server.onlinePlayers.map { it.name }.filter { it.lowercase().startsWith(args.first().lowercase()) }
            else -> null
        }
    }
}
