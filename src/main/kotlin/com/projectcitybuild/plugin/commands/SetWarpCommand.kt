package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.SerializableLocation
import com.projectcitybuild.features.warps.usecases.CreateWarpUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import com.projectcitybuild.plugin.exceptions.InvalidCommandArgumentsException
import org.bukkit.entity.Player
import org.bukkit.plugin.java.annotation.command.Command
import javax.inject.Inject

@Command(
    name = "setwarp",
    desc = "Creates a warp at the current position and direction",
    usage = "/setwarp <name>",
)
class SetWarpCommand @Inject constructor(
    private val createWarp: CreateWarpUseCase,
) : SpigotCommand {

    override val label = "setwarp"
    override val permission = "pcbridge.warp.create"
    override val usageHelp = "/setwarp <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }
        val player = input.sender as? Player
        if (player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        val warpName = input.args.first()
        val result = createWarp.createWarp(
            name = warpName,
            location = SerializableLocation.fromLocation(player.location)
        )
        when (result) {
            is Success -> input.sender.send().success("Created warp for $warpName")
            is Failure -> input.sender.send().error(
                when (result.reason) {
                    CreateWarpUseCase.FailureReason.WARP_ALREADY_EXISTS -> "A warp for $warpName already exists"
                }
            )
        }
    }
}
