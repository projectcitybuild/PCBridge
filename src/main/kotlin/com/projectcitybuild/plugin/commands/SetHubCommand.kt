package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.exceptions.CannotInvokeFromConsoleException
import com.projectcitybuild.core.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.features.hub.usecases.SetHubUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import javax.inject.Inject

class SetHubCommand @Inject constructor(
    private val setHub: SetHubUseCase,
) : SpigotCommand {

    override val label = "sethub"
    override val permission = "pcbridge.hub.set"
    override val usageHelp = "/sethub"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            throw CannotInvokeFromConsoleException()
        }
        if (input.args.isNotEmpty()) {
            throw InvalidCommandArgumentsException()
        }

        setHub.execute(location = input.player.location)

        input.sender.send().success("Destination of /hub has been set")
    }
}
