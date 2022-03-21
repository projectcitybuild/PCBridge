package com.projectcitybuild.features.teleporthistory.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandInput
import com.projectcitybuild.repositories.LastKnownLocationRepositoy
import com.projectcitybuild.integrations.shared.crossteleport.LocationTeleporter
import org.bukkit.entity.Player
import javax.inject.Inject

class BackCommand @Inject constructor(
    private val lastKnownLocationRepositoy: LastKnownLocationRepositoy,
    private val locationTeleporter: LocationTeleporter,
): SpigotCommand {

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

        val lastKnownLocation = lastKnownLocationRepositoy.get(input.sender.uniqueId)
        if (lastKnownLocation == null) {
            input.sender.send().error("No last known location")
            return
        }

        val result = locationTeleporter.teleport(
            player = input.sender,
            destination = lastKnownLocation.location,
            destinationName = "/back",
        )
        if (result is Failure) {
            when (result.reason) {
                LocationTeleporter.FailureReason.WORLD_NOT_FOUND ->
                    input.sender.send().error("Could not find world")
            }
        }
    }
}
