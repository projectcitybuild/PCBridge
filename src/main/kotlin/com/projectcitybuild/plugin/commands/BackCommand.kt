package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.exceptions.CannotInvokeFromConsoleException
import com.projectcitybuild.core.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.teleporthistory.usecases.BackUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import javax.inject.Inject

class BackCommand @Inject constructor(
    private val backUseCase: BackUseCase,
) : SpigotCommand {

    override val label: String = "back"
    override val permission = "pcbridge.tp.back"
    override val usageHelp = "/back"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            throw CannotInvokeFromConsoleException()
        }
        if (input.args.isNotEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        val result = backUseCase.teleportBack(input.player)

        when (result) {
            is Failure -> when (result.reason) {
                BackUseCase.FailureReason.WORLD_NOT_FOUND
                -> input.sender.send().error("Could not find world")

                BackUseCase.FailureReason.NO_LAST_LOCATION
                -> input.sender.send().error("No last known location")
            }
            is Success -> {
                input.sender.send().action("Teleporting back to previous location")
            }
        }
    }
}
