package com.projectcitybuild.features.hub.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.features.hub.repositories.HubRepository
import com.projectcitybuild.modules.teleport.LocationTeleporter
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandInput
import org.bukkit.entity.Player
import javax.inject.Inject

class HubCommand @Inject constructor(
    private val hubRepository: HubRepository,
    private val locationTeleporter: LocationTeleporter,
): SpigotCommand {

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

        val hubLocation = hubRepository.get()
        if (hubLocation == null) {
            input.sender.send().error("Hub has not been set")
            return
        }

        val result = locationTeleporter.teleport(
            player = input.sender,
            destination = hubLocation,
            destinationName = "hub",
        )
        if (result is Failure) {
            when (result.reason) {
                LocationTeleporter.FailureReason.WORLD_NOT_FOUND ->
                    input.sender.send().error("Could not find world")
            }
        }
    }
}
