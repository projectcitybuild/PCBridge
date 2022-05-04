package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.usecases.UnbanIPUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
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

        if (result is Failure) {
            input.sender.send().error(
                when (result.reason) {
                    UnbanIPUseCase.FailureReason.IP_NOT_BANNED -> "$targetIP is not currently banned"
                    UnbanIPUseCase.FailureReason.INVALID_IP -> "$targetIP is not a valid IP"
                }
            )
        }
        if (result is Success) {
            input.sender.send().success("IP $targetIP has been unbanned")
        }
    }
}