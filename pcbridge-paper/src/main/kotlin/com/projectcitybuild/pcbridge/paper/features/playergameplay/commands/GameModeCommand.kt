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
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class GameModeCommand(
    private val plugin: Plugin,
) : BrigadierCommand {
    override fun buildLiteral(): CommandNode
        = Commands.literal("gamemode")
            .requiresPermission(PermissionNode.PLAYER_GAMEPLAY)
            .then(
                Commands.argument("mode", ArgumentTypes.gameMode())
                    .executesSuspending(plugin, ::execute)
            )
            .then(
                Commands.argument("player", SingleOnlinePlayerArgument(plugin.server))
                    .then(
                        Commands.argument("mode", ArgumentTypes.gameMode())
                            .executesSuspending(plugin, ::execute)
                    )
            )
            .build()

    private suspend fun execute(context: CommandContext) = context.traceSuspending {
        val sender = context.source.sender
        val gameMode = context.getArgument("mode", GameMode::class.java)
        val player = context.getOptionalArgument("player", Player::class.java)
            ?: context.source.requirePlayer()

        player.gameMode = gameMode
        player.sendRichMessage(l10n.yourGameModeChangedTo(gameMode.name))

        if (player != sender) {
            sender.sendRichMessage(l10n.playerGameModeChanged(player.name, gameMode.name))
        }
    }
}
