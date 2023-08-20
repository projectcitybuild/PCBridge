package com.projectcitybuild.modules.moderation.bans.commands

import com.projectcitybuild.modules.moderation.bans.actions.UnbanUUID
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.entity.Player

class UnbanCommand(
    private val unbanUUID: UnbanUUID,
) {
    suspend fun execute(commandSender: Player, targetPlayerName: String) {
        val result = unbanUUID.unban(
            targetPlayerName= targetPlayerName,
            unbannerUUID = commandSender.uniqueId,
        )
        if (result is Failure) {
            commandSender.send().error(
                when (result.reason) {
                    UnbanUUID.FailureReason.PlayerDoesNotExist -> "Could not find UUID for $targetPlayerName. This player likely doesn't exist"
                    UnbanUUID.FailureReason.PlayerNotBanned -> "$targetPlayerName is not currently banned"
                }
            )
        }
    }
}
