package com.projectcitybuild.pcbridge.features.warnings.commands

import com.projectcitybuild.pcbridge.features.warnings.repositories.PlayerWarningRepository
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import org.bukkit.command.CommandSender

class WarningAcknowledgeCommand(
    private val warningRepository: PlayerWarningRepository,
) : SpigotCommand<WarningAcknowledgeCommand.Args> {
    override val label = "warning"

    override val usage = CommandHelpBuilder()

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        warningRepository.acknowledge(args.id)
        sender.sendMessage("Warning acknowledged and hidden")
    }

    data class Args(
        val id: Int,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                return Args(id = args[0].toInt())
            }
        }
    }
}
