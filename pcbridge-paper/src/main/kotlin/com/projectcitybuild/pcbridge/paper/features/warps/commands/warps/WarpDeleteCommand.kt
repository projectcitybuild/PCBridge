package com.projectcitybuild.pcbridge.paper.features.warps.commands.warps

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceCommand
import com.projectcitybuild.pcbridge.paper.features.warps.events.WarpDeleteEvent
import com.projectcitybuild.pcbridge.paper.features.warps.repositories.WarpRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Server
import org.bukkit.plugin.Plugin

class WarpDeleteCommand(
    private val plugin: Plugin,
    private val warpRepository: WarpRepository,
    private val server: Server,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("delete")
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

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = traceCommand(context) {
        val warpName = context.getArgument("name", String::class.java)

        warpRepository.delete(name = warpName)

        server.pluginManager.callEvent(WarpDeleteEvent())

        context.source.sender.sendMessage(
            Component.text("$warpName warp deleted")
                .color(NamedTextColor.GREEN),
        )
    }
}
