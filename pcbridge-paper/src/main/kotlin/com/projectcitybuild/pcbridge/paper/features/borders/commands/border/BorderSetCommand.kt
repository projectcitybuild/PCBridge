package com.projectcitybuild.pcbridge.paper.features.borders.commands.border

import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.then
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

class BorderSetCommand(
    private val borderSetRectangleCommand: BorderSetRectangleCommand,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("set")
            .then(command = borderSetRectangleCommand)
            .build()
    }
}