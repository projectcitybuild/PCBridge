package com.projectcitybuild.pcbridge.paper.architecture.commands

import com.mojang.brigadier.context.CommandContext
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.Tracer
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.common.AttributesBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

suspend fun <S: CommandSourceStack> CommandContext<S>.scoped(
    tracer: Tracer,
    block: suspend (CommandContext<S>) -> Unit,
) {
    val commandLiteral = input.split(" ").first()
    val attributes = Attributes.builder()
        .putSender(source.sender)
        .put("input", input)

    tracer.trace("command.$commandLiteral", attributes.build()) {
        runCatching { block(this) }.onFailure { e ->
            CommandExceptionHandler.catch(source.sender, e)
        }
    }
}

fun <S: CommandSourceStack> CommandContext<S>.scopedSync(
    tracer: Tracer,
    block: (CommandContext<S>) -> Unit,
) {
    val commandLiteral = input.split(" ").first()
    val attributes = Attributes.builder()
        .putSender(source.sender)
        .put("input", input)

    tracer.traceSync("command.$commandLiteral", attributes.build()) {
        runCatching { block(this) }.onFailure { e ->
            CommandExceptionHandler.catch(source.sender, e)
        }
    }
}

private fun AttributesBuilder.putSender(sender: CommandSender): AttributesBuilder {
    val player = sender as? Player
    put("sender_name", sender.name)
    put("player_location", player?.location.toString())
    put("player_uuid", player?.uniqueId.toString())
    return this
}