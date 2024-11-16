package com.projectcitybuild.pcbridge.paper.features.builds.commands

import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildListCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage")
class BuildsCommand(
    private val buildListCommand: BuildListCommand,
) {
    fun buildLiteral(plugin: Plugin): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("builds")
            .then(buildListCommand.buildLiteral(plugin))
            .then(
                Commands.literal("create")
                    .executes { context ->
                        context.source.sender.sendMessage("create")
                        com.mojang.brigadier.Command.SINGLE_SUCCESS
                    }
            )
            .then(
                Commands.literal("move")
                    .executes { context ->
                        context.source.sender.sendMessage("move")
                        com.mojang.brigadier.Command.SINGLE_SUCCESS
                    }
            )
            .then(
                Commands.literal("rename")
                    .executes { context ->
                        context.source.sender.sendMessage("rename")
                        com.mojang.brigadier.Command.SINGLE_SUCCESS
                    }
            )
            .executes { context ->
                context.source.sender.sendMessage("root")
                com.mojang.brigadier.Command.SINGLE_SUCCESS
            }
            .build()
    }
}
