package com.projectcitybuild.features.teleporting.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.teleporting.usecases.TPToggleUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import org.bukkit.command.CommandSender
import javax.inject.Inject

class TPToggleCommand @Inject constructor(
    private val tpToggleUseCase: TPToggleUseCase,
) : SpigotCommand {

    override val label = "tptoggle"
    override val permission = "pcbridge.tp.toggle"
    override val usageHelp = "/tptoggle [on|off]"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        val desiredState = when (input.args.firstOrNull()?.lowercase()) {
            "on" -> true
            "off" -> false
            null -> null
            else -> throw InvalidCommandArgumentsException()
        }

        val result = tpToggleUseCase.toggle(input.player.uniqueId, desiredState)
        if (result is Failure) {
            input.sender.send().error(
                when (result.reason) {
                    TPToggleUseCase.FailureReason.ALREADY_TOGGLED_ON -> "Already allowing teleports"
                    TPToggleUseCase.FailureReason.ALREADY_TOGGLED_OFF -> "Already disallowing teleports"
                }
            )
        }
        if (result is Success) {
            input.sender.send().success(
                if (result.value) "Players can now teleport to or summon you"
                else "Players can no longer teleport to or summon you"
            )
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> listOf("on", "off")
            else -> null
        }
    }
}
