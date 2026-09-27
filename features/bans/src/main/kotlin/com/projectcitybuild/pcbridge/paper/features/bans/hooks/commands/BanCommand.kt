package com.projectcitybuild.pcbridge.paper.features.bans.hooks.commands

import com.projectcitybuild.pcbridge.paper.core.libs.permissions.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.bans.bansTracer
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.dialogs.CreateBanDialog
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.platform.paper.extensions.onlinePlayer
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.arguments.OnlinePlayerNameArgument
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class BanCommand(
    private val plugin: Plugin,
    private val server: Server,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("ban")
            .requiresPermission(PermissionNode.BANS_MANAGE)
            .then(
                Commands.argument("player", OnlinePlayerNameArgument(server))
                    .executesSuspending(plugin, ::execute),
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

            val dialog = CreateBanDialog.build(playerName)
            context.source.sender.showDialog(dialog)
        }
}
