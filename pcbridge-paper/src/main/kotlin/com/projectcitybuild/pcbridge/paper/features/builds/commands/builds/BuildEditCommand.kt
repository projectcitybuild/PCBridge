package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

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
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage")
class BuildEditCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("edit")
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

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = traceCommand(context) {
        val name = context.getArgument("name", String::class.java)
        val player = context.source.executor as? Player

        checkNotNull(player) { "Only a player can use this command" }

        val build = buildRepository.get(name)
        checkNotNull(build) { "Build not found" }

        val actions = listOf("name", "description", "lore")
        val actionsRow = actions.joinToString(separator = " ") { field ->
            val command = "/builds set ${build.id} $field "
            "[<white><underlined><click:suggest_command:'$command'><hover:show_text:'$command'>${field.uppercase()}</hover></click></underlined></white>]"
        }
        context.source.sender.sendMessage(
            MiniMessage.miniMessage().deserialize("<gray>Click a field to edit:<newline>$actionsRow</gray>")
        )
    }
}