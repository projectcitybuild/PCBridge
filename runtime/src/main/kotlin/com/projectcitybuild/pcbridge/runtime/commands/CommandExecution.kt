package com.projectcitybuild.pcbridge.runtime.commands

import com.mojang.brigadier.context.CommandContext
import com.projectcitybuild.pcbridge.core.observability.tracing.Tracer
import io.papermc.paper.command.brigadier.CommandSourceStack

suspend fun <S : CommandSourceStack> CommandContext<S>.runCommandTracedSafely(
    tracer: Tracer,
    block: suspend (CommandContext<S>) -> Unit,
) {
    runCommandSafely(source.sender) {
        traced(tracer, block)
    }
}

fun <S : CommandSourceStack> CommandContext<S>.runCommandTracedSafelySync(
    tracer: Tracer,
    block: (CommandContext<S>) -> Unit,
) {
    runCommandSafelySync(source.sender) {
        tracedSync(tracer, block)
    }
}
