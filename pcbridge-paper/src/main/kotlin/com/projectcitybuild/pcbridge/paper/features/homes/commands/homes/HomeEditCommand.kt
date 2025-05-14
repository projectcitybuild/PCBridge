package com.projectcitybuild.pcbridge.paper.features.homes.commands.homes

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.homes.repositories.HomeRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class HomeEditCommand(
    private val plugin: Plugin,
    private val homeRepository: HomeRepository,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("edit")
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, ::suggest)
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun suggest(
        context: CommandContext<CommandSourceStack>,
        suggestions: SuggestionsBuilder,
    ) {
        val player = context.source.executor as? Player
            ?: return

        val input = suggestions.remaining.lowercase()

        homeRepository.names(playerUUID = player.uniqueId)
            .filter { it.name.startsWith(input) }
            .map { it.name }
            .forEach(suggestions::suggest)
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val name = context.getArgument("name", String::class.java)
        val player = context.source.executor as? Player
        checkNotNull(player) { "Only a player can use this command" }

        val home = homeRepository.get(player.uniqueId, name)
        checkNotNull(home) { "Home ($name) not found" }

        val component = Component.text("[name]", NamedTextColor.WHITE)
            .decorate(TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.suggestCommand("/homes setfield ${home.id} name "))
            .hoverEvent(HoverEvent.showText(Component.text("/homes setfield ${home.id} name")))

        context.source.sender.sendMessage(
            Component.text("Click a field to edit:", NamedTextColor.GRAY)
                .appendNewline()
                .append(component)
        )
    }
}