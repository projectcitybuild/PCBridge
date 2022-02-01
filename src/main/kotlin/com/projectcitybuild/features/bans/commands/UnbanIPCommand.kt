package com.projectcitybuild.features.bans.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.bans.usecases.unbanip.UnbanIPUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import javax.inject.Inject

class UnbanIPCommand @Inject constructor(
    private val unbanIPUseCase: UnbanIPUseCase,
): BungeecordCommand {

    override val label = "unbanip"
    override val permission = "pcbridge.ban.unbanip"
    override val usageHelp = "/unbanip <ip>"

    override suspend fun execute(input: BungeecordCommandInput) {
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