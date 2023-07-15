package com.projectcitybuild.modules.warps.commands

import com.projectcitybuild.modules.warps.actions.DeleteWarp
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.entity.Player

class DelWarpCommand(
    private val deleteWarp: DeleteWarp,
) {
    fun execute(commandSender: Player, warpName: String) {
        val result = deleteWarp.deleteWarp(warpName)

        when (result) {
            is Failure -> {
                commandSender.send().error(
                    when (result.reason) {
                        DeleteWarp.FailureReason.WARP_NOT_FOUND -> "Warp $warpName does not exist"
                    }
                )
            }
            is Success -> commandSender.send().success("Warp $warpName deleted")
        }
    }
}
