package com.projectcitybuild.pcbridge.runtime.commands

import com.mojang.brigadier.context.CommandContext
import com.projectcitybuild.pcbridge.core.observability.tracing.Tracer
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.common.AttributesBuilder
import io.opentelemetry.api.trace.SpanKind
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

suspend fun <S : CommandSourceStack> CommandContext<S>.traced(
    tracer: Tracer,
    block: suspend (CommandContext<S>) -> Unit,
) {
    val commandLiteral = input.split(" ").first()
    val attributes =
        Attributes.builder()
            .putSender(source.sender)
            .put("input", input)

    tracer.trace(
        operation = "command.$commandLiteral",
        spanKind = SpanKind.SERVER,
        attributes = attributes.build(),
    ) {
        block(this)
    }
}

fun <S : CommandSourceStack> CommandContext<S>.tracedSync(
    tracer: Tracer,
    block: (CommandContext<S>) -> Unit,
) {
    val commandLiteral = input.split(" ").first()
    val attributes =
        Attributes.builder()
            .putSender(source.sender)
            .put("input", input)

    tracer.traceSync(
        operation = "command.$commandLiteral",
        spanKind = SpanKind.SERVER,
        attributes = attributes.build(),
    ) {
        block(this)
    }
}

private fun AttributesBuilder.putSender(sender: CommandSender): AttributesBuilder {
    val player = sender as? Player
    put("sender_name", sender.name)
    put("player_location", player?.location.toString())
    put("player_uuid", player?.uniqueId.toString())
    return this
}
