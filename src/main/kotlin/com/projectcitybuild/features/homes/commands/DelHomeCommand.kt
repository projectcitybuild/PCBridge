package com.projectcitybuild.features.homes.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.homes.usecases.DeleteHomeUseCase
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.spigot.environment.SpigotCommand
import com.projectcitybuild.platforms.spigot.environment.SpigotCommandInput
import com.projectcitybuild.repositories.HomeRepository
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import javax.inject.Inject

class DelHomeCommand @Inject constructor(
    private val deleteHomeUseCase: DeleteHomeUseCase,
    private val homeRepository: HomeRepository,
): SpigotCommand {

    override val label: String = "delwarp"
    override val permission = "pcbridge.warp.delete"
    override val usageHelp = "/delwarp <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.sender !is Player) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val homeName = input.args.first()

        val result = deleteHomeUseCase.deleteHome(input.sender.uniqueId, homeName)
        when (result) {
            is Failure -> input.sender.send().error(
                when (result.reason) {
                    DeleteHomeUseCase.FailureReason.HOME_NOT_FOUND -> "Home $homeName does not exist"
                }
            )
            is Success -> input.sender.send().success("Home $homeName deleted")
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
