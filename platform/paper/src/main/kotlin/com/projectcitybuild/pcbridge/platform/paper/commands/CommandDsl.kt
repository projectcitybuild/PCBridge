package com.projectcitybuild.pcbridge.platform.paper.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.projectcitybuild.pcbridge.core.permissions.PermissionNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

@DslMarker
annotation class CommandDsl

@CommandDsl
class CommandBuilder internal constructor(
    private val builder: LiteralArgumentBuilder<CommandSourceStack>,
) {
    /**
     * Describes this root command for command suggestions and help.
     */
    var description: String = ""

    /**
     * Applies a permission-node check to this command.
     */
    fun requiresPermission(vararg permissions: PermissionNode) {
        builder.requires { context ->
            permissions.all { context.sender.hasPermission(it.node) }
        }
    }

    /**
     * Applies a permission-string check to this command.
     */
    fun requiresPermission(permission: String) {
        builder.requires { context ->
            context.sender.hasPermission(permission)
        }
    }

    /**
     * Executes this command with a synchronous Brigadier handler.
     */
    fun executes(command: Command<CommandSourceStack>) {
        builder.executes(command)
    }

    /**
     * Executes this command with a suspending handler.
     */
    fun executesSuspending(
        executor: CommandTaskExecutor,
        block: suspend (PaperCommandContext) -> Unit,
    ) {
        builder.executes { context ->
            executor.execute { block(context) }
            Command.SINGLE_SUCCESS
        }
    }

    /**
     * Executes this command using the plugin's coroutine dispatcher.
     */
    fun executesSuspending(
        plugin: Plugin,
        block: suspend (PaperCommandContext) -> Unit,
    ) {
        executesSuspending(PluginCommandTaskExecutor(plugin), block)
    }

    /**
     * Declares a literal subcommand.
     */
    fun literal(
        name: String,
        block: CommandBuilder.() -> Unit = {},
    ) {
        val child = CommandBuilder(Commands.literal(name))
        child.block()

        builder.then(child.builder)
    }

    /**
     * Adds an existing command.
     */
    fun then(command: BrigadierCommand) {
        builder.then(command.literal())
    }

    /**
     * Adds a preconfigured Brigadier argument.
     */
    fun <T> then(argument: RequiredArgumentBuilder<CommandSourceStack, T>) {
        builder.then(argument)
    }

    /**
     * Declares a typed Brigadier argument.
     */
    fun <T : Any> argument(
        name: String,
        type: ArgumentType<T>,
        block: RequiredArgumentBuilder<CommandSourceStack, T>.() -> Unit,
    ) {
        val argument = Commands.argument(name, type)
        argument.block()

        builder.then(argument)
    }

    /**
     * Declares a typed command argument.
     */
    fun <T : Any> argument(
        argument: CommandArgument<T>,
        block: RequiredArgumentBuilder<CommandSourceStack, T>.() -> Unit,
    ) {
        argument(argument.name, argument.type, block)
    }

    /**
     * Declares a nested typed Brigadier argument.
     */
    fun <T : Any, U : Any> RequiredArgumentBuilder<CommandSourceStack, T>.argument(
        name: String,
        type: ArgumentType<U>,
        block: RequiredArgumentBuilder<CommandSourceStack, U>.() -> Unit,
    ) {
        val argument = Commands.argument(name, type)
        argument.block()

        this.then(argument)
    }

    /**
     * Declares a nested typed command argument.
     */
    fun <T : Any, U : Any> RequiredArgumentBuilder<CommandSourceStack, T>.argument(
        argument: CommandArgument<U>,
        block: RequiredArgumentBuilder<CommandSourceStack, U>.() -> Unit,
    ) {
        argument(argument.name, argument.type, block)
    }

    /**
     * Escape hatch for Brigadier functionality not covered by
     * this DSL.
     */
    fun brigadier(block: LiteralArgumentBuilder<CommandSourceStack>.() -> Unit) {
        builder.block()
    }

    internal fun build() = CommandDefinition(
        node = builder.build(),
        description = description,
    )
}

data class CommandDefinition(
    val node: PaperCommandNode,
    val description: String,
)

/**
 * Declares a root command.
 */
fun command(
    name: String,
    block: CommandBuilder.() -> Unit = {},
): CommandDefinition {
    val command = CommandBuilder(Commands.literal(name))
    command.block()
    return command.build()
}
