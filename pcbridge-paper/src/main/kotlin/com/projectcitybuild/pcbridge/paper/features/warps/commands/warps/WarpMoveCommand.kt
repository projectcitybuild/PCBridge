package com.projectcitybuild.pcbridge.paper.features.warps.commands.warps

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class WarpMoveCommand(
    private val plugin: Plugin,
    private val warpRepository: WarpRepository,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("move")
            .requiresPermission(PermissionNode.WARP_MANAGE)
            .then(
                Commands.argument("name", StringArgumentType.string())
                    .suggestsSuspending(plugin, ::suggestWarp)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun suggestWarp(
        context: CommandContext<CommandSourceStack>,
        suggestions: SuggestionsBuilder,
    ) {
        val name = suggestions.remaining.lowercase()

        return warpRepository.all()
            .filter { it.name.lowercase().startsWith(name) }
            .map { it.name }
            .forEach(suggestions::suggest)
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val player = context.source.requirePlayer()
        val warpName = context.getArgument("name", String::class.java)

        val location = player.location
        warpRepository.move(
            name = warpName,
            world = location.world.name,
            x = location.x,
            y = location.y,
            z = location.z,
            pitch = location.pitch,
            yaw = location.yaw,
        )
        context.source.sender.sendRichMessage(
            "<green>$warpName warp moved</green>",
        )
    }
}
