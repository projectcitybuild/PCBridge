package com.projectcitybuild.pcbridge.paper.features.bans.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.pcbmanage.ManageUrlGenerator
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.OnlinePlayerNameArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class BanCommand(
    private val plugin: Plugin,
    private val server: Server,
    private val manageUrlGenerator: ManageUrlGenerator,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("ban")
            .requiresPermission(PermissionNode.BANS_MANAGE)
            .then(
                Commands.argument("player", OnlinePlayerNameArgument(server))
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val playerName = context.getArgument("player", String::class.java)

        val url = manageUrlGenerator.byPlayerUuid(
            playerName = playerName,
            path = "manage/player-bans/create"
        )

        val miniMessage = MiniMessage.miniMessage()
        val sender = context.source.sender
        sender.sendMessage(
            miniMessage.deserialize(
                "<gray>Click the link below to create a ban for this player</gray>"
            )
        )
        sender.sendMessage(
            miniMessage.deserialize(
            "<click:OPEN_URL:$url><aqua><underlined>$url</underlined></aqua></click>"
            )
        )
    }
}
