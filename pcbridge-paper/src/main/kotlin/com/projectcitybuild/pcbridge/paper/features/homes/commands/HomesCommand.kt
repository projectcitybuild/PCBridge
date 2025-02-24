package com.projectcitybuild.pcbridge.paper.features.homes.commands

import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.then
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeCreateCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeListCommand
import com.projectcitybuild.pcbridge.paper.features.homes.commands.homes.HomeMoveCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

class HomesCommand(
    private val homeCreateCommand: HomeCreateCommand,
    private val homeDeleteCommand: HomeDeleteCommand,
    private val homeListCommand: HomeListCommand,
    private val homeMoveCommand: HomeMoveCommand,
): BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("homes")
            .then(command = homeCreateCommand)
            .then(command = homeDeleteCommand)
            .then(command = homeListCommand)
            .then(command = homeMoveCommand)
            // TODO: can we use a Redirect here? Would be good to allow a page arg
            .requiresPermission(PermissionNode.HOMES_USE)
            .executes(homeListCommand.buildLiteral().command)
            .build()
    }
}
