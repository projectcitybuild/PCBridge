package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.entities.SerializableLocation
import com.projectcitybuild.features.warps.usecases.CreateWarpUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import org.bukkit.entity.Player
import javax.inject.Inject

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
        val location = SerializableLocation.fromLocation(player.location)

        val result = createWarp.createWarp(warpName, location)

        if (result is Failure) {
            when (result.reason) {
                CreateWarpUseCase.FailureReason.WARP_ALREADY_EXISTS
                -> input.sender.send().error("A warp for $warpName already exists")
            }
            return
        }

        input.sender.send().success("Created warp for $warpName")
    }
}
