package com.projectcitybuild.pcbridge.features.pim.hooks.commands

import com.projectcitybuild.pcbridge.features.pim.PimPermissions
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpGrantCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpRevokeCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpStatusCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.roles.RolesDebugCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.commands.then
import io.papermc.paper.command.brigadier.Commands

class PimCommand(
    private val opGrantCommand: OpGrantCommand,
    private val opRevokeCommand: OpRevokeCommand,
    private val opStatusCommand: OpStatusCommand,
    private val rolesDebugCommand: RolesDebugCommand,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = Commands
        .literal("pim")
        .requiresPermission(PimPermissions.OP)
        .then(
            Commands
                .literal("op")
                .then(command = opGrantCommand)
                .then(command = opRevokeCommand)
                .then(command = opStatusCommand),
        ).then(
            Commands
                .literal("roles")
                .then(command = rolesDebugCommand),
        ).build()
}
