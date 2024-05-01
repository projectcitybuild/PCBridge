package com.projectcitybuild.features.warps.commands

import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.features.warps.commands.warps.WarpListCommand
import com.projectcitybuild.features.warps.commands.warps.WarpRenameArgs
import com.projectcitybuild.features.warps.commands.warps.WarpRenameCommand
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.support.messages.CommandHelpBuilder
import com.projectcitybuild.support.spigot.ArgsParser
import com.projectcitybuild.support.spigot.SpigotCommand
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class WarpsCommand(
    private val warpRepository: WarpRepository,
    private val audiences: BukkitAudiences,
    private val config: Config<PluginConfig>,
): SpigotCommand<WarpsCommand.Args> {
    override val usage: CommandHelpBuilder
        get() = CommandHelpBuilder()
            .command(
                label = "/warps list",
                description = "shows all available warps",
                permission = "pcbridge.warp.list"
            )
            .command(
                label = "/warps rename",
                description = "renames the given warp",
                permission = "pcbridge.warp.manage"
            )

    override suspend fun run(sender: CommandSender, command: Command, args: Args) {
        when (args.command) {
            "list" -> WarpListCommand(
                warpRepository = warpRepository,
                audiences = audiences,
                itemsPerPage = config.get().warps.itemsPerPage,
            ).run(
                sender = sender,
                command = command,
                args = WarpListCommand.Args.Parser()
                    .tryParse(args.remainingArgs),
            )
            "rename" -> WarpRenameCommand(
                argsParser = WarpRenameArgs(),
                warpRepository = warpRepository,
                audiences = audiences,
            ).onCommand(
                sender = sender,
                args = args.remainingArgs,
            )
            else -> displayUsage(sender, audiences)
        }
        return
    }

    data class Args(
        val command: String,
        val remainingArgs: List<String>,
    ) {
        class Parser: ArgsParser<Args> {
            override fun tryParse(args: List<String>): Args? {
                if (args.isEmpty()) {
                    return null
                }
                return Args(
                    command = args[0],
                    remainingArgs = args.drop(1),
                )
            }
        }
    }
}