package com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.warps.WarpCreateCommand
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.warps.WarpDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.warps.WarpListCommand
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.warps.WarpMoveCommand
import com.projectcitybuild.pcbridge.paper.features.warps.hooks.commands.warps.WarpRenameCommand
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.architecture.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.then
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import io.papermc.paper.command.brigadier.Commands

class WarpsCommand(
    private val createCommand: WarpCreateCommand,
    private val deleteCommand: WarpDeleteCommand,
    private val listCommand: WarpListCommand,
    private val moveCommand: WarpMoveCommand,
    private val renameCommand: WarpRenameCommand,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("warps")
            .requiresPermission(PermissionNode.WARP_TELEPORT)
            .then(command = createCommand)
            .then(command = deleteCommand)
            .then(command = listCommand)
            .then(command = moveCommand)
            .then(command = renameCommand)
            // TODO: can we use a Redirect here? Would be good to allow a page arg
            .executes(listCommand.literal().command)
            .build()
    }
}
