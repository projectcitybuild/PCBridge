package com.projectcitybuild.pcbridge.features.bans.hooks.commands

import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import com.projectcitybuild.pcbridge.features.bans.bansTracer
import com.projectcitybuild.pcbridge.features.bans.hooks.dialogs.CreateBanDialog
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.extensions.onlinePlayer
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.arguments.OnlinePlayerNameArgument
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.dialog.DialogLike
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class BanCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val createDialog: (String?) -> DialogLike = CreateBanDialog::build,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("ban")
            .requiresPermission(PermissionNode.BANS_MANAGE)
            .then(
                Commands.argument("player", OnlinePlayerNameArgument(server))
                    .executesSuspending(taskExecutor, ::execute),
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) =
        context.scoped(bansTracer) {
            val inputPlayerName = context.getArgument("player", String::class.java)

            val playerName =
                inputPlayerName
                    ?.let { name -> server.onlinePlayer(name = name)?.name }
                    ?: inputPlayerName

            val dialog = createDialog(playerName)
            context.source.sender.showDialog(dialog)
        }
}
