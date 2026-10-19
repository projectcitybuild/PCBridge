package com.projectcitybuild.pcbridge.runtime.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.http.parsing.ResponseParserError
import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.runtime.cooldowns.CooldownException
import org.bukkit.command.CommandSender

class CommandExceptionHandler private constructor() {
    companion object {
        fun catch(
            sender: CommandSender,
            e: Throwable,
        ) = when (e) {
            is IllegalStateException -> sender.sendMessage(l10n.errors.commandErrorWithPrefix(e.message ?: "null"))
            is ResponseParserError ->
                when (e) {
                    is ResponseParserError.Validation ->
                        sender.sendMessage(e.message?.let(l10n.errors::commandErrorWithPrefix) ?: l10n.errors.commandErrorValidationFailed)
                    is ResponseParserError.NotFound ->
                        sender.sendMessage(e.message?.let(l10n.errors::commandErrorWithPrefix) ?: l10n.errors.commandErrorNotFound)
                    is ResponseParserError.Forbidden -> sender.sendMessage(l10n.errors.commandErrorNotPermitted)
                    is ResponseParserError.Conflict ->
                        sender.sendMessage(e.message?.let(l10n.errors::commandErrorWithPrefix) ?: l10n.errors.commandErrorConflict)
                }
            is CooldownException ->
                sender.sendMessage(
                    l10n.errors.commandErrorCooldown(e.remainingTime.inWholeMilliseconds),
                )
            else -> {
                sender.sendMessage(l10n.errors.commandUnexpectedError)
                logSync.error(e) { "Failed to execute command" }
            }
        }
    }
}
