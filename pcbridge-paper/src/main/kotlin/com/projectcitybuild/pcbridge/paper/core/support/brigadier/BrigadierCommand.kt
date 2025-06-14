package com.projectcitybuild.pcbridge.paper.core.support.brigadier

/**
 * Represents either a command or subcommand that can be
 * registered with Brigadier
 */
interface BrigadierCommand {
    val description: String?
        get() = null

    fun buildLiteral(): CommandNode
}