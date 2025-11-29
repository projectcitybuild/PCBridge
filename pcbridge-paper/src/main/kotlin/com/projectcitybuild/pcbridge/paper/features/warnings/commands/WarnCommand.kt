package com.projectcitybuild.pcbridge.paper.features.warnings.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.pcbmanage.ManageUrlGenerator
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.architecture.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.OnlinePlayerNameArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.architecture.commands.scoped
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.spigot.extensions.onlinePlayer
import com.projectcitybuild.pcbridge.paper.features.warnings.warningsTracer
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class WarnCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val manageUrlGenerator: ManageUrlGenerator,
): BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("warn")
            .requiresPermission(PermissionNode.WARNINGS_MANAGE)
            .then(
                Commands.argument("player", OnlinePlayerNameArgument(server))
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: PaperCommandContext) = context.scoped(warningsTracer) {
        val playerName = context.getArgument("player", String::class.java)

        val player = server.onlinePlayer(name = playerName)
        val lookup = player?.uniqueId?.toString() ?: playerName
        val url = manageUrlGenerator.path("manage/warnings/create?uuid=$lookup")

        val sender = context.source.sender
        sender.sendRichMessage(
            "<gray>Click the link below to create a warning for this player</gray>"
        )
        sender.sendRichMessage(
            "<click:OPEN_URL:$url><aqua><underlined>$url</underlined></aqua></click>"
        )
    }
}
