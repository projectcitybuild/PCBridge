package com.projectcitybuild.pcbridge.paper.core.support.brigadier

import com.mojang.brigadier.context.CommandContext
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender

suspend fun CommandContext<CommandSourceStack>.traceSuspending(
    block: suspend (CommandContext<CommandSourceStack>) -> Unit,
) {
    runCatching { block(this) }.onFailure(::catch)
}

fun CommandContext<CommandSourceStack>.trace(
    block: (CommandContext<CommandSourceStack>) -> Unit,
) {
    runCatching { block(this) }.onFailure(::catch)
}

private fun CommandContext<CommandSourceStack>.catch(e: Throwable) {
    when (e) {
        is IllegalStateException -> source.sender.sendError("Error: ${e.message}")
        is ResponseParser.ValidationError -> source.sender.sendError("Error: ${e.message}")
        else -> {
            source.sender.sendError("An unexpected error occurred")
            throw e // Bubble it up to the error reporter
        }
    }
}

private fun CommandSender.sendError(message: String) {
    sendMessage(
        MiniMessage.miniMessage().deserialize("<red>$message</red>")
    )
}