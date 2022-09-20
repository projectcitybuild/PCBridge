package com.projectcitybuild.plugin.commands

import com.projectcitybuild.features.chat.usecases.ToggleBadge
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.command.CommandSender
import javax.inject.Inject

class BadgeCommand @Inject constructor(
    private val toggleBadge: ToggleBadge,
) : SpigotCommand {

    override val label = "badge"
    override val permission = "pcbridge.chat.badge"
    override val usageHelp = "/badge <on|off>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val shouldDisableBadge = when (input.args.first().lowercase()) {
            "on" -> false
            "off" -> true
            else -> throw InvalidCommandArgumentsException()
        }
        toggleBadge.execute(
            willBeDisabled = shouldDisableBadge,
            playerUUID = input.player.uniqueId,
        )
        if (shouldDisableBadge) {
            input.sender.send().success("Your chat badge has been turned off")
        } else {
            input.sender.send().success("Your chat badge has been turned on")
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> listOf("on", "off")
            else -> null
        }
    }
}
