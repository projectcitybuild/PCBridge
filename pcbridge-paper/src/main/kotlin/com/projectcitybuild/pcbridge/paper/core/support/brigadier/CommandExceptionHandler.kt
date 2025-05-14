package com.projectcitybuild.pcbridge.paper.core.support.brigadier

import com.mojang.brigadier.context.CommandContext
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParserError
import com.projectcitybuild.pcbridge.paper.core.libs.cooldowns.CooldownException
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender

suspend fun <S: CommandSourceStack> CommandContext<S>.traceSuspending(
    block: suspend (CommandContext<S>) -> Unit,
) {
    runCatching { block(this) }.onFailure { e ->
        CommandExceptionHandler.catch(source.sender, e)
    }
}

fun <S: CommandSourceStack> CommandContext<S>.trace(
    block: (CommandContext<S>) -> Unit,
) {
    runCatching { block(this) }.onFailure { e ->
        CommandExceptionHandler.catch(source.sender, e)
    }
}

class CommandExceptionHandler private constructor() {
    companion object {
        fun catch(sender: CommandSender, e: Throwable) = when (e) {
            is IllegalStateException -> sender.sendError("Error: ${e.message}")
            is ResponseParserError -> when (e) {
                is ResponseParserError.Validation -> sender.sendError("Error: ${e.message}")
                is ResponseParserError.NotFound -> sender.sendError("Error: Not found")
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
