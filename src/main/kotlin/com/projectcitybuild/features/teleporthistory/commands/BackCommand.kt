package com.projectcitybuild.features.teleporthistory.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.teleporthistory.usecases.BackUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandInput
import org.bukkit.entity.Player
import javax.inject.Inject

class BackCommand @Inject constructor(
    private val backUseCase: BackUseCase,
) : SpigotCommand {

    override val label: String = "back"
    override val permission = "pcbridge.tp.back"
    override val usageHelp = "/back"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.sender !is Player) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.isNotEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val result = backUseCase.teleportBack(input.sender)

        when (result) {
            is Failure -> input.sender.send().error(result.reason.errorMessage())
            is Success -> input.sender.send().action("Teleporting back to previous location")
        }
    }
}

private fun BackUseCase.FailureReason.errorMessage(): String {
    return when (this) {
        BackUseCase.FailureReason.WORLD_NOT_FOUND -> "Could not find world"
        BackUseCase.FailureReason.NO_LAST_LOCATION -> "No last known location"
    }
}
