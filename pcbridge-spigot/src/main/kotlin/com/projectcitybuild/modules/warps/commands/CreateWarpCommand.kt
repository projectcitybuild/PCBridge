package com.projectcitybuild.modules.warps.commands

import com.projectcitybuild.entities.SerializableLocation
import com.projectcitybuild.modules.warps.actions.CreateWarp
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.entity.Player

class CreateWarpCommand(
    private val createWarp: CreateWarp,
) {
    fun execute(commandSender: Player, warpName: String) {
        val result = createWarp.createWarp(
            name = warpName,
            location = SerializableLocation.fromLocation(commandSender.location)
        )
        when (result) {
            is Success -> commandSender.send().success("Created warp for $warpName")
            is Failure -> commandSender.send().error(
                when (result.reason) {
                    CreateWarp.FailureReason.WARP_ALREADY_EXISTS -> "A warp for $warpName already exists"
                }
            )
        }
    }
}
