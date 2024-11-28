package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage")
class BuildSetCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("set")
            .then(
                Commands.argument("id", IntegerArgumentType.integer())
                    .then(
                        Commands.argument("field", StringArgumentType.word())
                            .suggests { _, suggestions ->
                                suggestions.suggest("description")
                                suggestions.buildFuture()
                            }
                            .then(
                                Commands.argument("name", StringArgumentType.greedyString())
                                    .suggestsSuspending(plugin, ::suggestDescription)
                                    .executesSuspending(plugin, ::execute)
                            )
                    )
            )
            .build()
    }

    private suspend fun suggestDescription(
        context: CommandContext<CommandSourceStack>,
        suggestions: SuggestionsBuilder,
    ) {
        val id = suggestions.remaining

        // TODO
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = traceCommand(context) {
        val name = context.getArgument("field", String::class.java)
        val player = context.source.executor as? Player

        checkNotNull(player) { "Only a player can use this command" }

        // TODO
    }
}