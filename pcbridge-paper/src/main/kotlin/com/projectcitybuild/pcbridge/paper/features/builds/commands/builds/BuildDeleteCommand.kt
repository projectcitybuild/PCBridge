package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class BuildDeleteCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("delete")
            .requiresPermission(PermissionNode.BUILDS_MANAGE)
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, ::suggestBuild)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun suggestBuild(
        context: CommandContext<CommandSourceStack>,
        suggestions: SuggestionsBuilder,
    ) {
        val name = suggestions.remaining

        buildRepository.names(prefix = name)
            .forEach(suggestions::suggest)
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val name = context.getArgument("name", String::class.java)
        val player = context.source.executor as? Player

        checkNotNull(player) { "Only a player can use this command" }

        buildRepository.delete(
            name = name,
            player = player,
        )
        context.source.sender.sendRichMessage("<green>${name} deleted</green>")
    }
}