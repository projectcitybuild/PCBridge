package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.mojang.brigadier.arguments.IntegerArgumentType
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
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage")
class BuildSetCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("set")
            .requiresPermission(PermissionNode.BUILDS_MANAGE)
            .then(
                Commands.argument("id", IntegerArgumentType.integer())
                    .then(
                        Commands.argument("field", StringArgumentType.word())
                            .suggests { _, suggestions ->
                                // TODO: clean this up
                                suggestions.suggest("description")
                                suggestions.suggest("name")
                                suggestions.suggest("lore")
                                suggestions.buildFuture()
                            }
                            .then(
                                Commands.argument("value", StringArgumentType.greedyString())
                                    .suggestsSuspending(plugin, ::suggestDescription)
                                    .executesSuspending(plugin, ::execute)
                            )
                            .executesSuspending(plugin, ::execute)
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
        val field = context.getArgument("field", String::class.java)
        val id = context.getArgument("id", Int::class.java)
        val value = runCatching { context.getArgument("value", String::class.java) }.getOrElse { "" }
        val player = context.source.executor as? Player

        checkNotNull(player) { "Only a player can use this command" }

        val editableField = BuildRepository.EditableField.valueOf(field.uppercase())
        buildRepository.set(
            id = id,
            player = player,
            field = editableField,
            value = value,
        )

        context.source.sender.sendMessage(
            MiniMessage.miniMessage().deserialize("<green>Build updated</green>")
        )
    }
}