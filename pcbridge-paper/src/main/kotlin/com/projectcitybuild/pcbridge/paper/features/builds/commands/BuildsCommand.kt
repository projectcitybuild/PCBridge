package com.projectcitybuild.pcbridge.paper.features.builds.commands

import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildCreateCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildListCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

@Suppress("UnstableApiUsage")
class BuildsCommand(
    private val buildListCommand: BuildListCommand,
    private val buildCreateCommand: BuildCreateCommand,
) {
    fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("builds")
            .then(buildListCommand.buildLiteral())
            .then(buildCreateCommand.buildLiteral())
            .build()
    }
}
