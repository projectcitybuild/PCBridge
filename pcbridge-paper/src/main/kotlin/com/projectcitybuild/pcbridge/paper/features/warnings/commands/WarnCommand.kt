package com.projectcitybuild.pcbridge.paper.features.warnings.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.pcbmanage.ManageUrlGenerator
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.OnlinePlayerNameArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class WarnCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val manageUrlGenerator: ManageUrlGenerator,
): BrigadierCommand {
    override fun buildLiteral(): CommandNode {
        return Commands.literal("warn")
            .requiresPermission(PermissionNode.WARNINGS_MANAGE)
            .then(
                Commands.argument("player", OnlinePlayerNameArgument(server))
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext) = context.traceSuspending {
        val playerName = context.getArgument("player", String::class.java)

        val url = manageUrlGenerator.byPlayerUuid(
            playerName = playerName,
            path = "manage/warnings/create"
        )

        val sender = context.source.sender
        sender.sendRichMessage(
            "<gray>Click the link below to create a warning for this player</gray>"
        )
        sender.sendRichMessage(
            "<click:OPEN_URL:$url><aqua><underlined>$url</underlined></aqua></click>"
        )
    }
}
