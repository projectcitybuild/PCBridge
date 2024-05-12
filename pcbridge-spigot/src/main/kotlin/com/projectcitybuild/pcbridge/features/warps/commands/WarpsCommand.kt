package com.projectcitybuild.pcbridge.features.warps.commands

import com.projectcitybuild.pcbridge.core.config.Config
import com.projectcitybuild.pcbridge.features.warps.commands.warps.WarpCreateCommand
import com.projectcitybuild.pcbridge.features.warps.commands.warps.WarpDeleteCommand
import com.projectcitybuild.pcbridge.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.features.warps.commands.warps.WarpListCommand
import com.projectcitybuild.pcbridge.features.warps.commands.warps.WarpMoveCommand
import com.projectcitybuild.pcbridge.features.warps.commands.warps.WarpRenameCommand
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.support.spigot.UnauthorizedCommandException
import com.projectcitybuild.pcbridge.support.tryValueOf
import org.bukkit.Server
import org.bukkit.command.CommandSender

class WarpsCommand(
    private val warpRepository: WarpRepository,
    private val config: Config,
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
                itemsPerPage = config.get().warps.itemsPerPage,
            ).run(
                sender = sender,
                args = WarpListCommand.Args.Parser()
                    .parse(args.remainingArgs),
            )

            Args.Command.Create -> WarpCreateCommand(
                warpRepository = warpRepository,
                server = server,
            ).run(
                sender = sender,
                args = WarpCreateCommand.Args.Parser()
                    .parse(args.remainingArgs),
            )

            Args.Command.Delete -> WarpDeleteCommand(
                warpRepository = warpRepository,
                server = server,
            ).run(
                sender = sender,
                args = WarpDeleteCommand.Args.Parser()
                    .parse(args.remainingArgs),
            )

            Args.Command.Move -> WarpMoveCommand(
                warpRepository = warpRepository,
            ).run(
                sender = sender,
                args = WarpMoveCommand.Args.Parser()
                    .parse(args.remainingArgs),
            )

            Args.Command.Rename -> WarpRenameCommand(
                warpRepository = warpRepository,
            ).run(
                sender = sender,
                args = WarpRenameCommand.Args.Parser()
                    .parse(args.remainingArgs),
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
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                val command = tryValueOf<Command>(args[0].replaceFirstChar { it.uppercase() })
                    ?: throw BadCommandUsageException()

                return Args(
                    command = command,
                    remainingArgs = args.drop(1),
                )
            }
        }
    }
}