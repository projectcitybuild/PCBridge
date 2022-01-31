package com.projectcitybuild.features.warps.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.features.warps.usecases.warp.WarpUseCase
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandInput
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import javax.inject.Inject

class WarpCommand @Inject constructor(
    private val warpUseCase: WarpUseCase,
    private val warpRepository: WarpRepository,
    private val config: PlatformConfig,
): SpigotCommand {

    override val label: String = "warp"
    override val permission = "pcbridge.warp.use"
    override val usageHelp = "/warp <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }
        if (input.sender !is Player) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        val targetWarpName = input.args.first()
        val result = warpUseCase.warp(
            targetWarpName,
            config.get(PluginConfig.SPIGOT_SERVER_NAME),
            input.sender
        )

        when (result) {
            is Failure -> {
                input.sender.send().error(
                    when (result.reason) {
                        WarpUseCase.FailureReason.WARP_NOT_FOUND -> "Warp $targetWarpName does not exist"
                        WarpUseCase.FailureReason.WORLD_NOT_FOUND -> "The target server is either offline or invalid"
                    }
                )
            }
            is Success -> {
                if (result.value.isSameServer) {
                    input.sender.send().action("Warped to ${result.value.warpName}")
                }
            }
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> warpRepository.all().map { it.name }
            args.size == 1 -> warpRepository.all().map { it.name }.filter { it.lowercase().startsWith(args.first()) }
            else -> null
        }
    }
}
