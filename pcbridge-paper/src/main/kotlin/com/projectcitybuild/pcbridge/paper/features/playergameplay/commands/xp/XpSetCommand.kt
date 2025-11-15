package com.projectcitybuild.pcbridge.paper.features.playergameplay.commands.xp

import com.mojang.brigadier.arguments.FloatArgumentType
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class XpSetCommand(
    private val plugin: Plugin,
) : BrigadierCommand {
    override fun buildLiteral(): CommandNode
        = Commands.literal("set")
            .then(
                Commands.argument("player", SingleOnlinePlayerArgument(plugin.server))
                    .then(
                        Commands.argument("amount", FloatArgumentType.floatArg())
                            .executesSuspending(plugin, ::execute)
                    )
            )
            .then(
                Commands.argument("amount", FloatArgumentType.floatArg())
                    .executesSuspending(plugin, ::execute)
            )
            .build()

    private suspend fun execute(context: CommandContext) = context.traceSuspending {
        val player = context.getOptionalArgument("player", Player::class.java)
            ?: context.source.requirePlayer()
        val amount = context.getArgument("amount", Float::class.java)

        player.exp = amount
    }
}
