package com.projectcitybuild.features.hub.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.features.hub.repositories.HubRepository
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandInput
import org.bukkit.entity.Player
import javax.inject.Inject

class SetHubCommand @Inject constructor(
    private val hubRepository: HubRepository,
    private val config: PlatformConfig,
): SpigotCommand {

    override val label = "sethub"
    override val permission = "pcbridge.hub.set"
    override val usageHelp = "/sethub"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.isNotEmpty()) {
            throw InvalidCommandArgumentsException()
        }
        if (input.sender !is Player) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        val playerLocation = input.sender.location
        val location = CrossServerLocation(
            serverName = config.get(ConfigKeys.SPIGOT_SERVER_NAME),
            playerLocation.world.name,
            playerLocation.x,
            playerLocation.y,
            playerLocation.z,
            playerLocation.pitch,
            playerLocation.yaw,
        )
        hubRepository.set(location)

        input.sender.send().success("Destination of /hub has been set")
    }
}
