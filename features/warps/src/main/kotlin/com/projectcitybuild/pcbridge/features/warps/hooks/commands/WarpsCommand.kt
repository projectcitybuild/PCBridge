package com.projectcitybuild.pcbridge.features.warps.hooks.commands

import com.projectcitybuild.pcbridge.features.warps.WarpsPermissions
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpCreateCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpDeleteCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpListCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpMoveCommand
import com.projectcitybuild.pcbridge.features.warps.hooks.commands.warps.WarpRenameCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.command

class WarpsCommand(
    private val createCommand: WarpCreateCommand,
    private val deleteCommand: WarpDeleteCommand,
    private val listCommand: WarpListCommand,
    private val moveCommand: WarpMoveCommand,
    private val renameCommand: WarpRenameCommand,
) : BrigadierCommand {
    override val commandDefinition by lazy {
        command("warps") {
            description = "Lists available warps"
            requiresPermission(WarpsPermissions.TELEPORT)
            then(createCommand)
            then(deleteCommand)
            then(listCommand)
            then(moveCommand)
            then(renameCommand)
            executes(listCommand.literal().command)
        }
    }
}
