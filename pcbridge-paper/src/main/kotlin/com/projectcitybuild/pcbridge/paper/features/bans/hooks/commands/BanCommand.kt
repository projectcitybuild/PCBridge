package com.projectcitybuild.pcbridge.paper.features.bans.hooks.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.OnlinePlayerNameArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.scopedSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.features.bans.banTracer
import com.projectcitybuild.pcbridge.paper.features.bans.hooks.dialogs.CreateBanDialog
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class BanCommand(
    private val plugin: Plugin,
    private val server: Server,
): BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("ban")
            .requiresPermission(PermissionNode.BANS_MANAGE)
            .then(
                Commands.argument("player", OnlinePlayerNameArgument(server))
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scopedSuspending(banTracer) {
        val inputPlayerName = context.getArgument("player", String::class.java)

        val playerName = inputPlayerName
            ?.let { name -> server.onlinePlayers.firstOrNull { it.name == name }?.name }
            ?: inputPlayerName

        val dialog = CreateBanDialog.build(playerName)
        context.source.sender.showDialog(dialog)
    }
}
