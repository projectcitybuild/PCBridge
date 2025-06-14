package com.projectcitybuild.pcbridge.paper.features.playergameplay.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class ExtinguishCommand(
    private val plugin: Plugin,
) : BrigadierCommand {
    override fun buildLiteral(): CommandNode
        = Commands.literal("extinguish")
            .requiresPermission(PermissionNode.PLAYER_GAMEPLAY)
            .then(
                Commands.argument("player", SingleOnlinePlayerArgument(plugin.server))
                    .executesSuspending(plugin, ::execute)
            )
            .executesSuspending(plugin, ::execute)
            .build()

    private suspend fun execute(context: CommandContext) = context.traceSuspending {
        val sender = context.source.sender
        val player = context.getOptionalArgument("player", Player::class.java)
            ?: context.source.requirePlayer()

        check(player.fireTicks > 0) { l10n.errorPlayerNotOnFire(player.name) }

        player.fireTicks = 0
        player.sendRichMessage(l10n.youHaveBeenExtinguished)

        if (player != sender) {
            sender.sendRichMessage(l10n.extinguishedPlayer(player.name))
        }
    }
}
