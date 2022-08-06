package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.warps.usecases.TeleportToWarpUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import com.projectcitybuild.plugin.exceptions.CannotInvokeFromConsoleException
import com.projectcitybuild.plugin.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.repositories.WarpRepository
import com.projectcitybuild.support.annotations.annotations.Command
import org.bukkit.command.CommandSender
import javax.inject.Inject

@Command(
    name = "warp",
    desc = "Teleports to a pre-defined location",
    usage = "/warp <name>",
)
class WarpCommand @Inject constructor(
    private val teleportToWarpUseCase: TeleportToWarpUseCase,
    private val warpRepository: WarpRepository,
) : SpigotCommand {

    override val label = "warp"
    override val permission = "pcbridge.warp.use"
    override val usageHelp = "/warp <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            throw CannotInvokeFromConsoleException()
        }
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetWarpName = input.args.first()
        val result = teleportToWarpUseCase.warp(
            player = input.player,
            targetWarpName = targetWarpName,
        )

        when (result) {
            is Failure -> input.sender.send().error(
                when (result.reason) {
                    TeleportToWarpUseCase.FailureReason.WARP_NOT_FOUND -> "Warp $targetWarpName does not exist"
                    TeleportToWarpUseCase.FailureReason.WORLD_NOT_FOUND -> "The target server is either offline or invalid"
                }
            )
            is Success -> input.sender.send().action("Warped to ${result.value.warpName}")
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> warpRepository.names()
            args.size == 1 -> warpRepository.names().filter { it.lowercase().startsWith(args.first()) }
            else -> null
        }
    }
}
