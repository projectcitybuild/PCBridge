package com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions

import com.github.shynixn.mccoroutine.bukkit.CoroutineTimings
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.plugin.Plugin

/**
 * Same as [RequiredArgumentBuilder.suggests] but bridged to a suspending function
 */
fun <S, T> RequiredArgumentBuilder<S, T>.suggestsSuspending(
    plugin: Plugin,
    block: suspend (CommandContext<S>, SuggestionsBuilder) -> Unit,
): RequiredArgumentBuilder<S, T> {
    return suggests { context, suggestions ->
        // Note: the SuggestionsBuilder object must be handled on the main dispatcher
        plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
            block(context, suggestions)
        }
        suggestions.buildFuture()
    }
}

/**
 * Same as [RequiredArgumentBuilder.executes] but bridged to a suspending function
 */
fun <S, T> RequiredArgumentBuilder<S, T>.executesSuspending(
    plugin: Plugin,
    block: suspend (CommandContext<S>) -> Unit,
): RequiredArgumentBuilder<S, T> {
    return executes { context ->
        plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
            block(context)
        }
        Command.SINGLE_SUCCESS
    }
}

/**
 * Same as [LiteralArgumentBuilder.executes] but bridged to a suspending function
 */
fun <S> LiteralArgumentBuilder<S>.executesSuspending(
    plugin: Plugin,
    block: suspend (CommandContext<S>) -> Unit,
): LiteralArgumentBuilder<S> {
    return executes { context ->
        plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
            block(context)
        }
        Command.SINGLE_SUCCESS
    }
}

fun LiteralArgumentBuilder<CommandSourceStack>.then(
    command: BrigadierCommand,
): LiteralArgumentBuilder<CommandSourceStack>
    = then(command.buildLiteral())

fun LiteralArgumentBuilder<CommandSourceStack>.requiresPermission(
    permission: PermissionNode,
): LiteralArgumentBuilder<CommandSourceStack> = requires { context ->
    context.sender.hasPermission(permission.node)
}

fun <S: CommandSourceStack, T> RequiredArgumentBuilder<S, T>.requiresPermission(
    permission: PermissionNode,
): RequiredArgumentBuilder<S, T> = requires { context ->
    context.sender.hasPermission(permission.node)
}

