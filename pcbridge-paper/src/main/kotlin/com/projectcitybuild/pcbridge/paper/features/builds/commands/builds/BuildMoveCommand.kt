package com.projectcitybuild.pcbridge.paper.features.builds.commands.builds

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.builds.repositories.BuildRepository
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.suggestsSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceCommand
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
            .requiresPermission(PermissionNode.BUILD_MANAGE)
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

        val location = player.location
        val build = buildRepository.update(
            name = name,
            player = player,
            world = location.world.name,
            location = player.location,
        )

        context.source.sender.sendMessage(
            MiniMessage.miniMessage().deserialize("<green>${build.name} moved to your location</green>")
        )
    }
}