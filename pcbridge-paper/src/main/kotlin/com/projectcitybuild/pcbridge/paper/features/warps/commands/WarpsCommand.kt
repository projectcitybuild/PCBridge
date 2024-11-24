package com.projectcitybuild.pcbridge.paper.features.warps.commands

import com.projectcitybuild.pcbridge.paper.core.datetime.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpCreateCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpListCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpMoveCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpRenameCommand
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import com.projectcitybuild.pcbridge.paper.core.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.paper.core.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.paper.core.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.paper.core.support.spigot.UnauthorizedCommandException
import com.projectcitybuild.pcbridge.paper.core.extensions.tryValueOf
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class WarpsCommand(
    private val warpRepository: WarpRepository,
    private val remoteConfig: RemoteConfig,
    private val server: Server,
    private val time: LocalizedTime,
) : SpigotCommand<WarpsCommand.Args> {
    override val label = "warps"

    override val usage =
        CommandHelpBuilder(usage = "/warps")
            .subcommand(
                label = "/warps list",
                description = "shows all available warps",
                permission = "pcbridge.warp.list",
            )
            .subcommand(
                label = "/warps create",
                description = "creates a warp at the given position (or current position)",
                permission = "pcbridge.warp.manage",
            )
            .subcommand(
                label = "/warps delete",
                description = "deletes the given warp",
                permission = "pcbridge.warp.manage",
            )
            .subcommand(
                label = "/warps move",
                description = "moves an existing warp to your current location",
                permission = "pcbridge.warp.manage",
            )
            .subcommand(
                label = "/warps rename",
                description = "renames the given warp",
                permission = "pcbridge.warp.manage",
            )

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        if (!sender.hasPermission("pcbridge.warp.manage") && !sender.hasPermission("pcbridge.warp.list")) {
            throw UnauthorizedCommandException()
        }
        when (args.command) {
            Args.Command.List ->
                WarpListCommand(
                    warpRepository = warpRepository,
                    itemsPerPage = remoteConfig.latest.config.warps.itemsPerPage,
                ).run(
                    sender = sender,
                    args =
                        WarpListCommand.Args.Parser()
                            .parse(args.remainingArgs),
                )

            Args.Command.Create ->
                WarpCreateCommand(
                    warpRepository = warpRepository,
                    server = server,
                    time = time,
                ).run(
                    sender = sender,
                    args =
                        WarpCreateCommand.Args.Parser()
                            .parse(args.remainingArgs),
                )

            Args.Command.Delete ->
                WarpDeleteCommand(
                    warpRepository = warpRepository,
                    server = server,
                ).run(
                    sender = sender,
                    args =
                        WarpDeleteCommand.Args.Parser()
                            .parse(args.remainingArgs),
                )

            Args.Command.Move ->
                WarpMoveCommand(
                    warpRepository = warpRepository,
                ).run(
                    sender = sender,
                    args =
                        WarpMoveCommand.Args.Parser()
                            .parse(args.remainingArgs),
                )

            Args.Command.Reload -> {
                warpRepository.reload()
                sender.sendMessage(
                    MiniMessage.miniMessage().deserialize("<green>Warps reloaded</green>")
                )
            }

            Args.Command.Rename ->
                WarpRenameCommand(
                    warpRepository = warpRepository,
                ).run(
                    sender = sender,
                    args =
                        WarpRenameCommand.Args.Parser()
                            .parse(args.remainingArgs),
                )
        }
    }

    override suspend fun tabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): List<String>? {
        if (args.isEmpty() || args.first().isEmpty()) {
            return if (sender.hasPermission("pcbridge.warp.manage")) {
                listOf("create", "delete", "list", "move", "reload", "rename")
            } else {
                listOf("list")
            }
        }
        when (args.first()) {
            "delete" -> {
                if (args.size != 2) return null
                val name = args[1]
                if (name.isEmpty()) {
                    return warpRepository.all().map { it.name }
                }
                return warpRepository.all()
                    .filter { it.name.lowercase().startsWith(name.lowercase()) }
                    .map { it.name }
            }
            "move" -> {
                if (args.size != 2) return null
                val name = args[1]
                if (name.isEmpty()) {
                    return warpRepository.all().map { it.name }
                }
                return warpRepository.all()
                    .filter { it.name.lowercase().startsWith(name.lowercase()) }
                    .map { it.name }
            }
            "rename" -> {
                if (args.size != 2) return null
                val name = args[1]
                if (name.isEmpty()) {
                    return warpRepository.all().map { it.name }
                }
                return warpRepository.all()
                    .filter { it.name.lowercase().startsWith(name.lowercase()) }
                    .map { it.name }
            }

            else -> return null
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
            Reload,
            Rename,
        }

        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                val command = if (args.isEmpty())
                    Command.List
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
