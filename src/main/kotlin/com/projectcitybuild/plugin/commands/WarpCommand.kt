package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.warps.usecases.TeleportToWarp
import com.projectcitybuild.repositories.WarpRepository
import com.projectcitybuild.support.spigot.commands.CannotInvokeFromConsoleException
import com.projectcitybuild.support.spigot.commands.InvalidCommandArgumentsException
import com.projectcitybuild.support.spigot.commands.SpigotCommand
import com.projectcitybuild.support.spigot.commands.SpigotCommandInput
import com.projectcitybuild.support.textcomponent.send
import org.bukkit.command.CommandSender

class WarpCommand(
    private val teleportToWarp: TeleportToWarp,
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
        val result = teleportToWarp.warp(
            player = input.player,
            targetWarpName = targetWarpName,
        )

        when (result) {
            is Failure -> input.sender.send().error(
                when (result.reason) {
                    TeleportToWarp.FailureReason.WARP_NOT_FOUND -> "Warp $targetWarpName does not exist"
                    TeleportToWarp.FailureReason.WORLD_NOT_FOUND -> "The target server is either offline or invalid"
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
