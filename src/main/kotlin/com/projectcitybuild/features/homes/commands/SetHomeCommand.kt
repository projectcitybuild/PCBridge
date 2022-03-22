package com.projectcitybuild.features.homes.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.features.homes.usecases.CreateHomeUseCase
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandInput
import org.bukkit.entity.Player
import javax.inject.Inject

class SetHomeCommand @Inject constructor(
    private val createHomeUseCase: CreateHomeUseCase,
    private val config: PlatformConfig,
): SpigotCommand {

    override val label = "sethome"
    override val permission = "pcbridge.homes.create"
    override val usageHelp = "/sethome <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.sender !is Player) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val homeName = input.args.first()
        val location = CrossServerLocation.fromLocation(
            serverName = config.get(ConfigKey.SPIGOT_SERVER_NAME),
            location = input.sender.location,
        )
        val result = createHomeUseCase.createHome(
            playerUUID = input.sender.uniqueId,
            homeName = homeName,
            location = location,
        )

        when (result) {
            is Failure -> input.sender.send().error(
                when (result.reason) {
                    CreateHomeUseCase.FailureReason.HOME_ALREADY_EXISTS -> "Home $homeName already exists"
                    CreateHomeUseCase.FailureReason.HOME_LIMIT_REACHED -> "You have reached the max home limit for this world"
                    CreateHomeUseCase.FailureReason.HOME_NOT_ALLOWED_IN_WORLD -> "Homes cannot be created in this world"
                }
            )
            is Success -> input.sender.send().success("Created home $homeName")
        }
    }
}
