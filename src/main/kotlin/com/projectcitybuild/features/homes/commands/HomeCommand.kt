package com.projectcitybuild.features.homes.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.features.homes.usecases.HomeUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandInput
import com.projectcitybuild.repositories.HomeRepository
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import javax.inject.Inject

class HomeCommand @Inject constructor(
    private val homeUseCase: HomeUseCase,
    private val homeRepository: HomeRepository,
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

        val targetHomeName = input.args.first()
        val result = homeUseCase.teleportToHome(
            player = input.sender,
            homeName = targetHomeName,
        )

        if (result is Failure) {
            input.sender.send().error(
                when (result.reason) {
                    HomeUseCase.FailureReason.NO_HOMES_REGISTERED -> "You do not have any homes"
                    HomeUseCase.FailureReason.HOME_NOT_FOUND -> "Home $targetHomeName does not exist"
                    HomeUseCase.FailureReason.WORLD_NOT_FOUND -> "The target server is either offline or invalid"
                }
            )
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        if (sender !is Player) {
            return null
        }
        return when {
            args.isEmpty() -> homeRepository.names(sender.uniqueId)
            args.size == 1 -> homeRepository.names(sender.uniqueId).filter { it.lowercase().startsWith(args.first()) }
            else -> null
        }
    }
}