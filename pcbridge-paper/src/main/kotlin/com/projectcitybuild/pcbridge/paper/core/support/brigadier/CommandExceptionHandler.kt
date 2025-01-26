package com.projectcitybuild.pcbridge.paper.core.support.brigadier

import com.mojang.brigadier.context.CommandContext
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.minimessage.MiniMessage
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
            is ResponseParser.ValidationError -> sender.sendError("Error: ${e.message}")
            else -> {
                sender.sendError("An unexpected error occurred")
                throw e // Bubble it up to the error reporter
            }
        }
    }
}

private fun CommandSender.sendError(message: String) {
    sendMessage(
        MiniMessage.miniMessage().deserialize("<red>$message</red>")
    )
}