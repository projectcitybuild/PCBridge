package com.projectcitybuild.pcbridge.paper.features.homes.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.then
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeCreateCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeLimitCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeListCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeMoveCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeRenameCommand
import io.papermc.paper.command.brigadier.Commands

class HomesCommand(
    private val homeCreateCommand: HomeCreateCommand,
    private val homeDeleteCommand: HomeDeleteCommand,
    private val homeListCommand: HomeListCommand,
    private val homeMoveCommand: HomeMoveCommand,
    private val homeLimitCommand: HomeLimitCommand,
    private val homeRenameCommand: HomeRenameCommand,
): BrigadierCommand {
    override fun buildLiteral(): CommandNode {
        return Commands.literal("homes")
            .requiresPermission(PermissionNode.HOMES_USE)
            .then(command = homeCreateCommand)
            .then(command = homeDeleteCommand)
            .then(command = homeListCommand)
            .then(command = homeMoveCommand)
            .then(command = homeLimitCommand)
            .then(command = homeRenameCommand)
            .executes(homeListCommand.buildLiteral().command)
            .build()
    }
}
