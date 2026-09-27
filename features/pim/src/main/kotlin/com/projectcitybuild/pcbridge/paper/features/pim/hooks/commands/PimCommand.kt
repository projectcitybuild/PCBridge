package com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands

import com.projectcitybuild.pcbridge.paper.core.libs.permissions.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.op.OpGrantCommand
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.op.OpRevokeCommand
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.op.OpStatusCommand
import com.projectcitybuild.pcbridge.paper.features.pim.hooks.commands.roles.RolesDebugCommand
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.then
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandNode
import io.papermc.paper.command.brigadier.Commands

class PimCommand(
    private val opGrantCommand: OpGrantCommand,
    private val opRevokeCommand: OpRevokeCommand,
    private val opStatusCommand: OpStatusCommand,
    private val rolesDebugCommand: RolesDebugCommand,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("pim")
            .requiresPermission(PermissionNode.PIM_OP)
            .then(
                Commands.literal("op")
                    .then(command = opGrantCommand)
                    .then(command = opRevokeCommand)
                    .then(command = opStatusCommand),
            )
            .then(
                Commands.literal("roles")
                    .then(command = rolesDebugCommand),
            )
            .build()
    }
}
