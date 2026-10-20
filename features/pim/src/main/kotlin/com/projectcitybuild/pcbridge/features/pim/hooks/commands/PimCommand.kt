package com.projectcitybuild.pcbridge.features.pim.hooks.commands

import com.projectcitybuild.pcbridge.features.pim.PimPermissions
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpGrantCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpRevokeCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.op.OpStatusCommand
import com.projectcitybuild.pcbridge.features.pim.hooks.commands.roles.RolesDebugCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.command

class PimCommand(
    private val opGrantCommand: OpGrantCommand,
    private val opRevokeCommand: OpRevokeCommand,
    private val opStatusCommand: OpStatusCommand,
    private val rolesDebugCommand: RolesDebugCommand,
) : BrigadierCommand {
    override val commandDefinition by lazy {
        command("pim") {
            description = "Opens player identity management commands"
            requiresPermission(PimPermissions.OP)

            literal("op") {
                then(opGrantCommand)
                then(opRevokeCommand)
                then(opStatusCommand)
            }
            literal("roles") {
                then(rolesDebugCommand)
            }
        }
    }
}
