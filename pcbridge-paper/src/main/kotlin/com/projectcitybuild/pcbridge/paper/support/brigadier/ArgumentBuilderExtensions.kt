package com.projectcitybuild.pcbridge.paper.support.brigadier

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
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
