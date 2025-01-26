package com.projectcitybuild.pcbridge.paper.core.support.brigadier

import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack

/**
 * Represents either a command or subcommand that can be
 * registered with Brigadier
 */
interface BrigadierCommand {
    val description: String?
        get() = null

    fun buildLiteral(): LiteralCommandNode<CommandSourceStack>
}