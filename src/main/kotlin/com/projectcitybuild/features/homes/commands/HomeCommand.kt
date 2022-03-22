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

    override val label: String = "home"
    override val permission = "pcbridge.homes.use"
    override val usageHelp = "/home <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.sender !is Player) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
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
                    HomeUseCase.FailureReason.WORLD_NOT_FOUND -> "The target world is unavailable or missing"
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