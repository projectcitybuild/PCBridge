package com.projectcitybuild.modules.moderation.warnings.commands

import com.projectcitybuild.features.warnings.usecases.AcknowledgeWarning
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.command.CommandSender

class WarningAcknowledgeCommand(
    private val acknowledgeWarning: AcknowledgeWarning,
) : SpigotCommand {

    override val label = "warning"
    override val permission = "pcbridge.warning.acknowledge"
    override val usageHelp = "/warning acknowledge <id>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size < 2 || input.args.first() != "acknowledge") {
            throw InvalidCommandArgumentsException()
        }
        val id = input.args[1].toIntOrNull()
            ?: throw InvalidCommandArgumentsException()

        input.sender.send().action("Acknowledging...")

        acknowledgeWarning.execute(warningId = id)

        input.sender.send().success("Warning acknowledged and hidden")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> listOf("acknowledge")
            else -> null
        }
    }
}
