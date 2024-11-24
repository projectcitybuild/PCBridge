package com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.commands

import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.paper.core.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.paper.core.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.paper.core.support.spigot.UnauthorizedCommandException
import com.projectcitybuild.pcbridge.paper.core.extensions.tryValueOf
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class ConfigCommand(
    private val remoteConfig: RemoteConfig,
) : SpigotCommand<ConfigCommand.Args> {
    override val label = "config"

    override val usage =
        CommandHelpBuilder(usage = "/config")
            .subcommand(
                label = "/config reload",
                description = "force reload the remote config",
                permission = "pcbridge.config.reload",
            )

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        if (!sender.hasPermission("pcbridge.config.reload")) {
            throw UnauthorizedCommandException()
        }
        when (args.command) {
            Args.Command.Reload -> {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Fetching config...</gray>"))
                remoteConfig.fetch()
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Remote config reloaded</green>"))
            }
        }
    }

    override suspend fun tabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): List<String>? {
        if (args.isEmpty() || args.first().isEmpty()) {
            if (sender.hasPermission("pcbridge.config.reload")) {
                return listOf("reload")
            }
        }
        return emptyList()
    }

    data class Args(
        val command: Command,
        val remainingArgs: List<String>,
    ) {
        enum class Command {
            Reload,
        }

        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                val command = if (args.isEmpty())
                    throw BadCommandUsageException()
                else
                    tryValueOf<Command>(args[0].replaceFirstChar { it.uppercase() })
                        ?: throw BadCommandUsageException()

                return Args(
                    command = command,
                    remainingArgs = args.drop(1),
                )
            }
        }
    }
}
