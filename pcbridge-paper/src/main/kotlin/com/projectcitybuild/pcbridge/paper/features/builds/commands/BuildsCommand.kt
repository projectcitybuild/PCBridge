package com.projectcitybuild.pcbridge.paper.features.builds.commands

import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildCreateCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildListCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildMoveCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildVoteCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.then
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildEditCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildSetCommand
import com.projectcitybuild.pcbridge.paper.features.builds.commands.builds.BuildUnvoteCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

@Suppress("UnstableApiUsage")
class BuildsCommand(
    private val buildCreateCommand: BuildCreateCommand,
    private val buildDeleteCommand: BuildDeleteCommand,
    private val buildEditCommand: BuildEditCommand,
    private val buildListCommand: BuildListCommand,
    private val buildMoveCommand: BuildMoveCommand,
    private val buildSetCommand: BuildSetCommand,
    private val buildUnvoteCommand: BuildUnvoteCommand,
    private val buildVoteCommand: BuildVoteCommand,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("builds")
            .then(command = buildCreateCommand)
            .then(command = buildDeleteCommand)
            .then(command = buildEditCommand)
            .then(command = buildListCommand)
            .then(command = buildMoveCommand)
            .then(command = buildSetCommand)
            .then(command = buildUnvoteCommand)
            .then(command = buildVoteCommand)
            .requiresPermission(PermissionNode.BUILDS_TELEPORT)
            .executes(buildListCommand.buildLiteral().command)
            .build()
    }
}
