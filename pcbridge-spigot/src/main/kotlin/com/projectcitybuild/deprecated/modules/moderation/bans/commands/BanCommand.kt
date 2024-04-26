package com.projectcitybuild.modules.moderation.bans.commands

import com.projectcitybuild.modules.moderation.bans.actions.BanUUID
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.entity.Player

class BanCommand(
    private val banUUID: BanUUID,
) {
    suspend fun execute(
        commandSender: Player,
        targetPlayerName: String,
        reason: String,
    ) {
        val result = banUUID.ban(
            targetPlayerName,
            bannerUUID = commandSender.uniqueId,
            bannerName = commandSender.name,
            reason
        )
        if (result is Failure) {
            commandSender.send().error(
                when (result.reason) {
                    BanUUID.FailureReason.PlayerDoesNotExist -> "Could not find UUID for $targetPlayerName. This player likely doesn't exist"
                    BanUUID.FailureReason.PlayerAlreadyBanned -> "$targetPlayerName is already banned"
                }
            )
        }
    }
}
