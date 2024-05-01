package com.projectcitybuild.features.warps.commands

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.data.repositories.WarpRepository
import com.projectcitybuild.features.warps.commands.warps.WarpListArgs
import com.projectcitybuild.features.warps.commands.warps.WarpListCommand
import com.projectcitybuild.features.warps.commands.warps.WarpRenameArgs
import com.projectcitybuild.features.warps.commands.warps.WarpRenameCommand
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.support.messages.CommandHelpBuilder
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class WarpsCommand(
    private val warpRepository: WarpRepository,
    private val audiences: BukkitAudiences,
    private val config: Config<PluginConfig>,
): SuspendingCommandExecutor {
    override suspend fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.isEmpty()) {
            displayUsage(sender)
            return true
        }
        when (args[0]) {
            "list" -> WarpListCommand(
                argsParser = WarpListArgs(),
                warpRepository = warpRepository,
                audiences = audiences,
                itemsPerPage = config.get().warps.itemsPerPage,
            ).onCommand(
                sender = sender,
                args = args.toList().drop(1),
            )
            "rename" -> WarpRenameCommand(
                argsParser = WarpRenameArgs(),
                warpRepository = warpRepository,
                audiences = audiences,
            ).onCommand(
                sender = sender,
                args = args.toList().drop(1),
            )
        }
        return true
    }

    private fun displayUsage(sender: CommandSender) {
        val message = CommandHelpBuilder()
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
            .build(hasPermission = sender::hasPermission)

        audiences.sender(sender).sendMessage(message)
    }
}
