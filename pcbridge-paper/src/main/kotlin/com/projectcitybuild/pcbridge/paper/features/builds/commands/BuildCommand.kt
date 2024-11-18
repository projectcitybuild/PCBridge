package com.projectcitybuild.pcbridge.paper.features.builds.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.features.builds.commands.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.support.brigadier.executesSuspending
import com.projectcitybuild.pcbridge.paper.support.brigadier.suggestsSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage")
class BuildCommand(
    private val plugin: Plugin,
    private val buildRepository: BuildRepository,
    private val server: Server,
) {
    fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("build")
            .then(
                Commands.argument("name", StringArgumentType.greedyString())
                    .suggestsSuspending(plugin, this::suggestBuild)
                    .executesSuspending(plugin, this::execute)
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

        val build = buildRepository.get(name = name)
        if (build == null) {
            context.source.sender.sendMessage(
                MiniMessage.miniMessage().deserialize("Build not found")
            )
            return
        }
        val world = server.getWorld(build.world)
        if (world == null) {
            context.source.sender.sendMessage(
                MiniMessage.miniMessage().deserialize("<red>Error: Could not find world ${build.world}</red>")
            )
            return
        }

        context.source.sender.sendMessage(
            MiniMessage.miniMessage().deserialize("<gray>Teleporting to $name...</gray>")
        )
        val location = Location(
            world,
            build.x,
            build.y,
            build.z,
            build.yaw,
            build.pitch,
        )
        context.source.executor?.teleportAsync(
            location,
            PlayerTeleportEvent.TeleportCause.COMMAND,
        )
    }
}
