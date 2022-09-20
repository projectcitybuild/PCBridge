package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.SerializableLocation
import com.projectcitybuild.features.warps.usecases.CreateWarp
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.entity.Player
import javax.inject.Inject

class SetWarpCommand @Inject constructor(
    private val createWarp: CreateWarp,
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
                    CreateWarp.FailureReason.WARP_ALREADY_EXISTS -> "A warp for $warpName already exists"
                }
            )
        }
    }
}
