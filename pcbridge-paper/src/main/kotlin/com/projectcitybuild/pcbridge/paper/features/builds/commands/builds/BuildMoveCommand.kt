package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.support.brigadier.executesSuspending
import com.projectcitybuild.pcbridge.paper.support.brigadier.suggestsSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage")
class BuildMoveCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("move")
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
        val name = suggestions.remaining.lowercase()

        // TODO: optimize with a Trie later
        buildRepository.names()
            .filter { it.lowercase().startsWith(name) }
            .forEach(suggestions::suggest)
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) {
        val name = context.getArgument("name", String::class.java)
        val player = context.source.executor as? Player

        val miniMessage = MiniMessage.miniMessage()
        if (player == null) {
            context.source.sender.sendMessage(
                miniMessage.deserialize("<red>Only a player can use this command</red>")
            )
            return
        }
        val location = player.location

        val build = try {
            buildRepository.update(
                name = name,
                player = player,
                world = location.world.name,
                location = player.location,
            )
        } catch (error: ResponseParser.ValidationError) {
            context.source.sender.sendMessage(
                miniMessage.deserialize("<red>Error: ${error.message}</red>")
            )
            return
        } catch (error: Exception) {
            context.source.sender.sendMessage(
                miniMessage.deserialize("<red>An unexpected error occurred</red>")
            )
            throw error
        }

        context.source.sender.sendMessage(
            miniMessage.deserialize("<green>${build.name} moved to your location</green>")
        )
    }
}