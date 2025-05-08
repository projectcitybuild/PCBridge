package com.projectcitybuild.pcbridge.paper.features.borders.commands

import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.then
import com.projectcitybuild.pcbridge.paper.features.borders.commands.border.BorderDeleteCommand
import com.projectcitybuild.pcbridge.paper.features.borders.commands.border.BorderSetCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

class BorderCommand(
    private val borderSetCommand: BorderSetCommand,
    private val borderDeleteCommand: BorderDeleteCommand,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("border")
            .requiresPermission(PermissionNode.BORDER_MANAGE)
            .then(command = borderSetCommand)
            .then(command = borderDeleteCommand)
            .build()
    }
}