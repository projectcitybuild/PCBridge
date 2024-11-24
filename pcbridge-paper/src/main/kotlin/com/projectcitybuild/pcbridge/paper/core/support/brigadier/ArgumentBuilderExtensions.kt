package com.projectcitybuild.pcbridge.paper.core.support.brigadier

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
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
        plugin.launch { block(context, suggestions) }
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
        plugin.launch { block(context) }
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
        plugin.launch { block(context) }
        Command.SINGLE_SUCCESS
    }
}

@Suppress("UnstableApiUsage")
fun LiteralArgumentBuilder<CommandSourceStack>.then(
    command: BrigadierCommand,
): LiteralArgumentBuilder<CommandSourceStack> {
    return then(command.buildLiteral())
}