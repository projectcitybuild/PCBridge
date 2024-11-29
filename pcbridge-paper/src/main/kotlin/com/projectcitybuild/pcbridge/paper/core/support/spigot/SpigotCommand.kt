package com.projectcitybuild.pcbridge.paper.core.support.spigot

import com.projectcitybuild.pcbridge.paper.core.support.messages.CommandHelpBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

@Deprecated("Use Brigadier instead")
interface SpigotCommand<T> {
    val label: String
    val usage: CommandHelpBuilder

    fun displayUsage(sender: CommandSender) {
        sender.sendMessage(
            usage.build(sender::hasPermission),
        )
    }

    suspend fun run(
        sender: CommandSender,
        args: T,
    )

    suspend fun tabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): List<String>? = emptyList()
}

/**
 * Transformer class to bridge Spigot command arguments (Array<out String>)
 * into a convenient data class (T) for commands to use. Provides additional
 * validation and formatting if necessary.
 *
 * Throws [BadCommandUsageException] if expected arguments are not present
 * Throws [IllegalStateException] if present arguments are invalid or malformed
 */
@Deprecated("Use Brigadier instead")
interface CommandArgsParser<T> {
    @Throws(IllegalStateException::class, BadCommandUsageException::class)
    fun parse(args: List<String>): T

    @Throws(IllegalStateException::class, BadCommandUsageException::class)
    fun parse(args: Array<out String>): T = parse(args.toList())
}

/**
 * Represents that the expected arguments for a command were not present
 */
class BadCommandUsageException : Exception()

/**
 * Represents that the player does not have the required permission
 * to execute a command
 */
class UnauthorizedCommandException : Exception()
