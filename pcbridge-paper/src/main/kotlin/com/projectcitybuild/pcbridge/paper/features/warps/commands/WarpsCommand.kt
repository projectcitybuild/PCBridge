package com.projectcitybuild.pcbridge.paper.features.warps.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.then
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpCreateCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpListCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpMoveCommand
import com.projectcitybuild.pcbridge.paper.features.warps.commands.warps.WarpRenameCommand
import io.papermc.paper.command.brigadier.Commands

class WarpsCommand(
    private val createCommand: WarpCreateCommand,
    private val deleteCommand: WarpDeleteCommand,
    private val listCommand: WarpListCommand,
    private val moveCommand: WarpMoveCommand,
    private val renameCommand: WarpRenameCommand,
) : BrigadierCommand {
    override fun buildLiteral(): CommandNode {
        return Commands.literal("warps")
            .requiresPermission(PermissionNode.WARP_TELEPORT)
            .then(command = createCommand)
            .then(command = deleteCommand)
            .then(command = listCommand)
            .then(command = moveCommand)
            .then(command = renameCommand)
            // TODO: can we use a Redirect here? Would be good to allow a page arg
            .executes(listCommand.buildLiteral().command)
            .build()
    }
}
