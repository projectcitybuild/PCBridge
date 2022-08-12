package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.warps.usecases.DeleteWarpUseCase
import com.projectcitybuild.repositories.WarpRepository
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.command.CommandSender
import javax.inject.Inject

class DelWarpCommand @Inject constructor(
    private val deleteWarpUseCase: DeleteWarpUseCase,
    private val warpRepository: WarpRepository,
) : SpigotCommand {

    override val label = "delwarp"
    override val permission = "pcbridge.warp.delete"
    override val usageHelp = "/delwarp <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val warpName = input.args.first()

        val result = deleteWarpUseCase.deleteWarp(warpName)
        when (result) {
            is Failure -> {
                input.sender.send().error(
                    when (result.reason) {
                        DeleteWarpUseCase.FailureReason.WARP_NOT_FOUND -> "Warp $warpName does not exist"
                    }
                )
            }
            is Success -> input.sender.send().success("Warp $warpName deleted")
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
