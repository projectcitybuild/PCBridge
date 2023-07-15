package com.projectcitybuild.modules.moderation.bans.commands

import com.projectcitybuild.modules.moderation.bans.actions.BanIP
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.Server
import org.bukkit.entity.Player

class BanIPCommand(
    private val server: Server,
    private val banIP: BanIP,
) {
    suspend fun execute(
        commandSender: Player,
        target: String,
        reason: String?,
    ) {
        val targetIP = server.onlinePlayers
            .firstOrNull { it.name.lowercase() == target.lowercase() }
            ?.address?.toString()
            ?: target

        val result = banIP.execute(
            ip = targetIP,
            bannerUUID = commandSender.uniqueId,
            bannerName = commandSender.name,
            reason = if (reason.isNullOrEmpty()) "No reason given" else reason,
        )
        when (result) {
            is Failure -> commandSender.send().error(
                when (result.reason) {
                    BanIP.FailureReason.IP_ALREADY_BANNED -> "$targetIP is already banned"
                    BanIP.FailureReason.INVALID_IP -> "$targetIP is not a valid IP"
                }
            )
            is Success -> commandSender.send().success("IP $targetIP has been banned")
        }
    }
}
