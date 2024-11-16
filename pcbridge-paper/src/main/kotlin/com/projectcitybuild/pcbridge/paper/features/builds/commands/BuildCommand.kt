package com.projectcitybuild.pcbridge.paper.features.builds.commands

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.features.builds.commands.repositories.BuildRepository
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage")
class BuildCommand(
    private val buildRepository: BuildRepository,
) {
    fun buildLiteral(plugin: Plugin): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("build")
            .then(
                Commands.argument("name", StringArgumentType.string())
                    .suggests { _, suggestions ->
                        plugin.launch {
                            buildRepository.names().forEach(suggestions::suggest)
                        }
                        suggestions.buildFuture()
                    }
                    .executes { context ->
                        val name = context.getArgument("name", String::class.java)
                        context.source.sender.sendMessage(name)
                        com.mojang.brigadier.Command.SINGLE_SUCCESS
                    }
            )
            .build()
    }
}
