package com.projectcitybuild.pcbridge.paper.features.player.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class IceCommand(
    private val plugin: Plugin,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack>
        = Commands.literal("ice")
            .requiresPermission(PermissionNode.PLAYER_GAMEPLAY)
            .then(
                Commands.argument("player", SingleOnlinePlayerArgument(plugin.server))
                    .then(
                        Commands.argument("seconds", DoubleArgumentType.doubleArg(0.0, 120.0))
                            .executesSuspending(plugin, ::execute)
                    )
                    .executesSuspending(plugin, ::execute)
            )
            .build()

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val player = context.getArgument("player", Player::class.java)
        val seconds = context.getOptionalArgument("seconds", Double::class.java) ?: 1.0
        val sender = context.source.sender

        check(seconds > 0) { l10n.errorSecondsMustBeGreaterThanZero }

        val ticks = (seconds * 20).toInt()
        player.freezeTicks = ticks
        player.sendRichMessage(l10n.youHaveBeenIced(seconds))

        if (player != sender) {
            sender.sendRichMessage(l10n.icedPlayer(player.name, seconds, ticks))
        }
    }
}
