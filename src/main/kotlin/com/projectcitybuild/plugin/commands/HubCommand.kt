package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.hub.usecases.HubTeleportUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import org.bukkit.entity.Player
import javax.inject.Inject

class HubCommand @Inject constructor(
    private val hubTeleport: HubTeleportUseCase,
) : SpigotCommand {

    override val label: String = "hub"
    override val permission = "pcbridge.hub.use"
    override val usageHelp = "/hub"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.isNotEmpty()) {
            throw InvalidCommandArgumentsException()
        }
        if (input.sender !is Player) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        val result = hubTeleport.execute(player = input.player)

        when (result) {
            is Failure -> input.sender.send().error(
                when (result.reason) {
                    HubTeleportUseCase.FailureReason.NO_HUB_EXISTS -> "Hub has not been set"
                    HubTeleportUseCase.FailureReason.WORLD_NOT_FOUND -> "Could not find world"
                }
            )
            is Success -> input.sender.send().action("Teleported to hub")
        }
    }
}
