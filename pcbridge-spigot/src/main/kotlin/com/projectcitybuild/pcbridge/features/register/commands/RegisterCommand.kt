package com.projectcitybuild.pcbridge.features.register.commands

import com.projectcitybuild.pcbridge.features.register.repositories.RegisterRepository
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RegisterCommand(
    private val registerRepository: RegisterRepository,
) : SpigotCommand<RegisterCommand.Args> {
    override val label = "register"

    override val usage = CommandHelpBuilder()

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        check(sender is Player) {
            "Only players can use this command"
        }
        registerRepository.sendCode(
            email = args.email,
            playerAlias = sender.name,
            playerUUID = sender.uniqueId,
        )
    }

    data class Args(
        val email: String,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                if (args.size > 1) {
                    throw BadCommandUsageException()
                }
                return Args(email = args[0])
            }
        }
    }
}
