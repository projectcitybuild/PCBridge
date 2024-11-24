package com.projectcitybuild.pcbridge.paper.core.support.brigadier

import com.mojang.brigadier.context.CommandContext
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.paper.core.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.paper.core.support.spigot.UnauthorizedCommandException
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender

@Suppress("UnstableApiUsage")
suspend fun traceCommand(
    context: CommandContext<CommandSourceStack>,
    block: suspend (CommandContext<CommandSourceStack>) -> Unit,
) {
    try {
        block(context)
    } catch (e: IllegalStateException) {
        context.source.sender.sendError("Error: ${e.message}")
    } catch (e: BadCommandUsageException) {
        // TODO
    } catch (e: UnauthorizedCommandException) {
        // TODO: is this still needed?
        context.source.sender.sendError("Error: You do not have permission to use this command")
    } catch (e: ResponseParser.ValidationError) {
        context.source.sender.sendError("Error: {${e.message}")
    } catch (e: Exception) {
        context.source.sender.sendError("An unexpected error occurred")

        // Rethrow so that it bubbles up to the error reporter
        throw e
    }
}

private fun CommandSender.sendError(message: String) {
    sendMessage(
        MiniMessage.miniMessage().deserialize("<red>$message</red>")
    )
}