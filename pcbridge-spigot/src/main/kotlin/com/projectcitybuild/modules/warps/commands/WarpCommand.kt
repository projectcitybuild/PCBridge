package com.projectcitybuild.modules.warps.commands

import com.projectcitybuild.modules.warps.actions.TeleportToWarp
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.entity.Player

class WarpCommand(
    private val teleportToWarp: TeleportToWarp,
) {
    fun execute(commandSender: Player, warpName: String) {
        val result = teleportToWarp.warp(
            player = commandSender,
            targetWarpName = warpName,
        )
        when (result) {
            is Failure -> commandSender.send().error(
                when (result.reason) {
                    TeleportToWarp.FailureReason.WARP_NOT_FOUND -> "Warp $warpName does not exist"
                    TeleportToWarp.FailureReason.WORLD_NOT_FOUND -> "The target server is either offline or invalid"
                }
            )
            is Success -> commandSender.send().action("Warped to ${result.value.warpName}")
        }
    }
}
