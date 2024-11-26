package com.projectcitybuild.pcbridge.paper.features.builds.commands

import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildCreateCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildListCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildMoveCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildVoteCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.then
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildUnvoteCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

@Suppress("UnstableApiUsage")
class BuildsCommand(
    private val buildListCommand: BuildListCommand,
    private val buildCreateCommand: BuildCreateCommand,
    private val buildMoveCommand: BuildMoveCommand,
    private val buildVoteCommand: BuildVoteCommand,
    private val buildUnvoteCommand: BuildUnvoteCommand,
    private val buildDeleteCommand: BuildDeleteCommand,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("builds")
            .then(command = buildListCommand)
            .then(command = buildCreateCommand)
            .then(command = buildMoveCommand)
            .then(command = buildVoteCommand)
            .then(command = buildUnvoteCommand)
            .then(command = buildDeleteCommand)
            .executes(buildListCommand.buildLiteral().command)
            .build()
    }
}
