package com.projectcitybuild.modules.moderation.bans.commands

import com.projectcitybuild.features.bans.usecases.UnbanIP
import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send

class UnbanIPCommand(
    private val unbanIP: UnbanIP,
) : SpigotCommand {

    override val label = "unbanip"
    override val permission = "pcbridge.ban.unbanip"
    override val usageHelp = "/unbanip <ip>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetIP = input.args.first()

        val result = unbanIP.execute(
            ip = targetIP,
            unbannerUUID = input.player.uniqueId,
            unbannerName = input.player.name,
        )

        when (result) {
            is Failure -> input.sender.send().error(
                when (result.reason) {
                    UnbanIP.FailureReason.IP_NOT_BANNED -> "$targetIP is not currently banned"
                    UnbanIP.FailureReason.INVALID_IP -> "$targetIP is not a valid IP"
                }
            )
            is Success -> input.sender.send().success("IP $targetIP has been unbanned")
        }
    }
}
