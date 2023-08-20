package com.projectcitybuild.modules.ranksync.commands

import com.projectcitybuild.modules.ranksync.actions.UpdatePlayerGroups
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.entity.Player

class SyncOtherCommand(
    private val updatePlayerGroups: UpdatePlayerGroups,
) {
    suspend fun execute(commandSender: Player, targetPlayer: Player) {
        val result = updatePlayerGroups.execute(targetPlayer.uniqueId)

        when (result) {
            is Failure -> commandSender.send().error(
                when (result.reason) {
                    UpdatePlayerGroups.FailureReason.ACCOUNT_NOT_LINKED
                    -> "Sync failed: Player does not have a linked PCB account"
                }
            )
            is Success -> {
                commandSender.send().success("$targetPlayer has been synchronized")
                targetPlayer.send().success("Your account groups have been synchronized")
            }
        }
    }
}
