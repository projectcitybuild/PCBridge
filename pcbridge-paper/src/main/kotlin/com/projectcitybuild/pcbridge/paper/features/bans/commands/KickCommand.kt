package com.projectcitybuild.pcbridge.paper.features.bans.commands

import co.aikar.timings.TimingsManager.url
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.libs.pcbmanage.ManageUrlGenerator
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.OnlinePlayerNameArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.plugin.Plugin

class KickCommand(
    private val plugin: Plugin,
    private val server: Server,
): BrigadierCommand {
    private val miniMessage = MiniMessage.miniMessage()

    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("kick")
            .requiresPermission(PermissionNode.BANS_MANAGE)
            .then(
                Commands.argument("player", SingleOnlinePlayerArgument(plugin.server))
                    .then(
                        Commands.argument("reason", StringArgumentType.greedyString())
                            .executesSuspending(plugin, ::execute)
                    )
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val player = context.getArgument("player", Player::class.java)
        val reason = context.getOptionalArgument("reason", String::class.java)

        player.kick(
            miniMessage.deserialize(reason ?: l10n.kickedFromServer),
            PlayerKickEvent.Cause.KICK_COMMAND,
        )
        server.broadcast(
            miniMessage.deserialize(
                if (!reason.isNullOrEmpty())
                    l10n.playerWasKickedForReason(player.name, reason)
                else
                    l10n.playerWasKicked(player.name)
            )
        )
    }
}
