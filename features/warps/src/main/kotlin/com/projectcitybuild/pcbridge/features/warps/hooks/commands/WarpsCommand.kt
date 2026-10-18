package com.projectcitybuild.pcbridge.features.warps.hooks.commands

import com.projectcitybuild.pcbridge.features.warps.WarpsPermissions
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpCreateCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpDeleteCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpListCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpMoveCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpRenameCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.commands.then
import io.papermc.paper.command.brigadier.Commands

class WarpsCommand(
    private val createCommand: WarpCreateCommand,
    private val deleteCommand: WarpDeleteCommand,
    private val listCommand: WarpListCommand,
    private val moveCommand: WarpMoveCommand,
    private val renameCommand: WarpRenameCommand,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = Commands
        .literal("warps")
        .requiresPermission(WarpsPermissions.TELEPORT)
        .then(command = createCommand)
        .then(command = deleteCommand)
        .then(command = listCommand)
        .then(command = moveCommand)
        .then(command = renameCommand)
        // TODO: can we use a Redirect here? Would be good to allow a page arg
        .executes(listCommand.literal().command)
        .build()
}
