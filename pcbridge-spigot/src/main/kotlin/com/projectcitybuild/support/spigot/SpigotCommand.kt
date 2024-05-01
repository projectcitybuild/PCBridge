package com.projectcitybuild.support.spigot

import com.projectcitybuild.support.messages.CommandHelpBuilder
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

interface SpigotCommand<T> {
    suspend fun run(sender: CommandSender, command: Command, args: T)

    val usage: CommandHelpBuilder

    fun displayUsage(sender: CommandSender, audiences: BukkitAudiences) {
        audiences.sender(sender)
            .sendMessage(usage.build(sender::hasPermission))
    }
}

/**
 * Transformer class to bridge Spigot command arguments (Array<out String>)
 * into a convenient data class (T) for commands to use. Provides additional
 * validation and formatting if necessary.
 *
 * Throws [BadCommandUsageException] if expected arguments are not present
 * Throws [IllegalStateException] if present arguments are invalid or malformed
 */
interface ArgsParser<T> {
    @Throws(IllegalStateException::class, BadCommandUsageException::class)
    fun tryParse(args: List<String>): T?

    @Throws(IllegalStateException::class, BadCommandUsageException::class)
    fun tryParse(args: Array<out String>): T?
        = tryParse(args.toList())
}

/**
 * Represents that the expected arguments for a command were not present
 */
class BadCommandUsageException: Exception()