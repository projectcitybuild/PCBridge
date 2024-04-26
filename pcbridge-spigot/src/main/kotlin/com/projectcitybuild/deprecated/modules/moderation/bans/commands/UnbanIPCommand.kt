package com.projectcitybuild.modules.moderation.bans.commands

import com.projectcitybuild.modules.moderation.bans.actions.UnbanIP
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.entity.Player

class UnbanIPCommand(
    private val unbanIP: UnbanIP,
) {
    suspend fun execute(commandSender: Player, targetIP: String) {
        val result = unbanIP.execute(
            ip = targetIP,
            unbannerUUID = commandSender.uniqueId,
            unbannerName = commandSender.name,
        )

        when (result) {
            is Failure -> commandSender.send().error(
                when (result.reason) {
                    UnbanIP.FailureReason.IP_NOT_BANNED -> "$targetIP is not currently banned"
                    UnbanIP.FailureReason.INVALID_IP -> "$targetIP is not a valid IP"
                }
            )
            is Success -> commandSender.send().success("IP $targetIP has been unbanned")
        }
    }
}
