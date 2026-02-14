package com.projectcitybuild.pcbridge.paper.architecture.commands

import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParserError
import com.projectcitybuild.pcbridge.paper.core.libs.cooldowns.CooldownException
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import org.bukkit.command.CommandSender

class CommandExceptionHandler private constructor() {
    companion object {
        fun catch(sender: CommandSender, e: Throwable) = when (e) {
            is IllegalStateException -> sender.sendError("Error: ${e.message}")
            is ResponseParserError -> when (e) {
                is ResponseParserError.Validation -> sender.sendError("Error: ${e.message ?: "Validation failed"}")
                is ResponseParserError.NotFound -> sender.sendError("Error: ${e.message ?: "Not Found"}")
                is ResponseParserError.Forbidden -> sender.sendError("Error: Not permitted to perform this action")
                is ResponseParserError.Conflict -> sender.sendError("Error: ${e.message ?: "Conflict"}")
            }
            is CooldownException -> sender.sendError("Error: Please wait ${e.remainingTime.inWholeMilliseconds} seconds before trying again")
            else -> {
                sender.sendError("An unexpected error occurred")
                logSync.error(e) { "Failed to execute command" }
            }
        }
    }
}

private fun CommandSender.sendError(message: String)
    = sendRichMessage("<red>$message</red>")
