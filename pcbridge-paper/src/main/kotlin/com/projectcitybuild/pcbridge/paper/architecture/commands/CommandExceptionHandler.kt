package com.projectcitybuild.pcbridge.paper.architecture.commands

import com.mojang.brigadier.context.CommandContext
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParserError
import com.projectcitybuild.pcbridge.paper.core.libs.cooldowns.CooldownException
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.Tracer
import com.projectcitybuild.pcbridge.paper.core.support.spigot.utilities.sanitized
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.sentry.ScopeType
import io.sentry.Sentry
import io.sentry.protocol.User
import org.bukkit.command.CommandSender

suspend fun <S: CommandSourceStack> CommandContext<S>.scopedSuspending(
    tracer: Tracer,
    block: suspend (CommandContext<S>) -> Unit,
) {
    tracer.trace("command.${command::class}") {
//        val sender = source.sender
//        scope.user = if (sender is org.bukkit.entity.Player) {
//            User().apply {
//                id = sender.uniqueId.toString()
//                username = sender.name
//                ipAddress = sender.address?.address?.sanitized()
//            }
//        } else null
        runCatching { block(this) }.onFailure { e ->
            CommandExceptionHandler.catch(source.sender, e)
        }
    }
}

fun <S: CommandSourceStack> CommandContext<S>.scoped(
    tracer: Tracer,
    block: (CommandContext<S>) -> Unit,
) {
    Sentry.configureScope(ScopeType.ISOLATION) { scope ->
        scope.setTag("command", command.toString())

        val sender = source.sender
        scope.user = if (sender is org.bukkit.entity.Player) {
            User().apply {
                id = sender.uniqueId.toString()
                username = sender.name
                ipAddress = sender.address?.address?.sanitized()
            }
        } else null

        runCatching { block(this) }.onFailure { e ->
            CommandExceptionHandler.catch(source.sender, e)
        }
    }
}

class CommandExceptionHandler private constructor() {
    companion object {
        fun catch(sender: CommandSender, e: Throwable) = when (e) {
            is IllegalStateException -> sender.sendError("Error: ${e.message}")
            is ResponseParserError -> when (e) {
                is ResponseParserError.Validation -> sender.sendError("Error: ${e.message ?: "Validation failed"}")
                is ResponseParserError.NotFound -> sender.sendError("Error: {${e.message ?: "Not Found"}")
                is ResponseParserError.Forbidden -> sender.sendError("Error: Not permitted to perform this action")
                is ResponseParserError.Conflict -> sender.sendError("Error: ${e.message ?: "Conflict"}")
            }
            is CooldownException -> sender.sendError("Error: Please wait ${e.remainingTime.inWholeMilliseconds} seconds before trying again")
            else -> {
                sender.sendError("An unexpected error occurred")
                throw e // Bubble it up to the error reporter
            }
        }
    }
}

private fun CommandSender.sendError(message: String)
    = sendRichMessage("<red>$message</red>")
