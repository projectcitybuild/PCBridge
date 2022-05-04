package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
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
        if (input.args.isNotEmpty()) {
            throw InvalidCommandArgumentsException()
        }
        if (input.isConsole) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        setHub.execute(location = input.player.location)

        input.sender.send().success("Destination of /hub has been set")
    }
}
