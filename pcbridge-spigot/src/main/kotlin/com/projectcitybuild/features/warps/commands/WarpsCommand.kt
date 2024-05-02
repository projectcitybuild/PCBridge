package com.projectcitybuild.features.warps.commands

import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.features.warps.commands.warps.WarpCreateCommand
import com.projectcitybuild.features.warps.commands.warps.WarpDeleteCommand
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.features.warps.commands.warps.WarpListCommand
import com.projectcitybuild.features.warps.commands.warps.WarpMoveCommand
import com.projectcitybuild.features.warps.commands.warps.WarpRenameCommand
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.support.messages.CommandHelpBuilder
import com.projectcitybuild.support.spigot.CommandArgsParser
import com.projectcitybuild.support.spigot.SpigotCommand
import com.projectcitybuild.support.spigot.UnauthorizedCommandException
import com.projectcitybuild.support.tryValueOf
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Server
import org.bukkit.command.CommandSender

class WarpsCommand(
    private val warpRepository: WarpRepository,
    private val audiences: BukkitAudiences,
    private val config: Config<PluginConfig>,
    private val server: Server,
): SpigotCommand<WarpsCommand.Args> {
    override val label = "warps"

    override val usage = CommandHelpBuilder()
        .subcommand(
            label = "/warps list",
            description = "shows all available warps",
            permission = "pcbridge.warp.list"
        )
        .subcommand(
            label = "/warps create",
            description = "creates a warp at the given position (or current position)",
            permission = "pcbridge.warp.manage"
        )
        .subcommand(
            label = "/warps delete",
            description = "deletes the given warp",
            permission = "pcbridge.warp.manage"
        )
        .subcommand(
            label = "/warps move",
            description = "moves an existing warp to your current location",
            permission = "pcbridge.warp.manage"
        )
        .subcommand(
            label = "/warps rename",
            description = "renames the given warp",
            permission = "pcbridge.warp.manage"
        )

    override suspend fun run(sender: CommandSender, args: Args) {
        if (!sender.hasPermission("pcbridge.warp.manage") && !sender.hasPermission("pcbridge.warp.list")) {
            throw UnauthorizedCommandException()
        }
        when (args.command) {
            Args.Command.List -> WarpListCommand(
                warpRepository = warpRepository,
                audiences = audiences,
                itemsPerPage = config.get().warps.itemsPerPage,
            ).run(
                sender = sender,
                args = WarpListCommand.Args.Parser()
                    .tryParse(args.remainingArgs),
            )

            Args.Command.Create -> WarpCreateCommand(
                warpRepository = warpRepository,
                audiences = audiences,
                server = server,
            ).run(
                sender = sender,
                args = WarpCreateCommand.Args.Parser()
                    .tryParse(args.remainingArgs),
            )

            Args.Command.Delete -> WarpDeleteCommand(
                warpRepository = warpRepository,
                audiences = audiences,
            ).run(
                sender = sender,
                args = WarpDeleteCommand.Args.Parser()
                    .tryParse(args.remainingArgs),
            )

            Args.Command.Move -> WarpMoveCommand(
                warpRepository = warpRepository,
                audiences = audiences,
            ).run(
                sender = sender,
                args = WarpMoveCommand.Args.Parser()
                    .tryParse(args.remainingArgs),
            )

            Args.Command.Rename -> WarpRenameCommand(
                warpRepository = warpRepository,
                audiences = audiences,
            ).run(
                sender = sender,
                args = WarpRenameCommand.Args.Parser()
                    .tryParse(args.remainingArgs),
            )
        }
    }

    data class Args(
        val command: Command,
        val remainingArgs: List<String>,
    ) {
        enum class Command {
            List,
            Create,
            Delete,
            Move,
            Rename,
        }
        class Parser: CommandArgsParser<Args> {
            override fun tryParse(args: List<String>): Args? {
                if (args.isEmpty()) {
                    return null
                }
                val command = tryValueOf<Command>(args[0].replaceFirstChar { it.uppercase() })
                    ?: return null

                return Args(
                    command = command,
                    remainingArgs = args.drop(1),
                )
            }
        }
    }
}