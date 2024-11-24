package com.projectcitybuild.pcbridge.paper.support.brigadier

import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack

@Suppress("UnstableApiUsage")
interface BrigadierCommand {
    fun buildLiteral(): LiteralCommandNode<CommandSourceStack>
}