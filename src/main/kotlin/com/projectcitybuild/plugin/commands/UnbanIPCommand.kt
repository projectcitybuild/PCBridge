package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.usecases.UnbanIPUseCase
import com.projectcitybuild.support.textcomponent.send
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import javax.inject.Inject

class UnbanIPCommand @Inject constructor(
    private val unbanIPUseCase: UnbanIPUseCase,
) : SpigotCommand {

    override val label = "unbanip"
    override val permission = "pcbridge.ban.unbanip"
    override val usageHelp = "/unbanip <ip>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetIP = input.args.first()

        val result = unbanIPUseCase.unbanIP(targetIP)

        when (result) {
            is Failure -> input.sender.send().error(
                when (result.reason) {
                    UnbanIPUseCase.FailureReason.IP_NOT_BANNED -> "$targetIP is not currently banned"
                    UnbanIPUseCase.FailureReason.INVALID_IP -> "$targetIP is not a valid IP"
                }
            )
            is Success -> input.sender.send().success("IP $targetIP has been unbanned")
        }
    }
}
