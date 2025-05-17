package com.projectcitybuild.pcbridge.paper.features.register.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.http.pcb.services.RegisterHttpService
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParserError
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class CodeCommand(
    private val plugin: Plugin,
    private val registerHttpService: RegisterHttpService,
) : BrigadierCommand {
    override val description: String = "Finishes account registration by verifying a code"

    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("code")
            .then(
                Commands.argument("code", StringArgumentType.string())
                    .executesSuspending(plugin, ::execute)
            )
            .executes { context ->
                context.source.sender.sendRichMessage(
                    "<red>Error: You did not specify a code</red><newline><gray>Example Usage: <bold>/code 123456</bold></gray>",
                )
                return@executes Command.SINGLE_SUCCESS
            }
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val code = context.getArgument("code", String::class.java)
        val sender = context.source.executor
        check(sender is Player) { "Only players can use this command" }

        try {
            registerHttpService.verifyCode(
                code = code,
                playerUUID = sender.uniqueId,
            )
            sender.sendRichMessage(
                "<green>Registration complete! Your account will be synced momentarily...</green>",
            )
        } catch (e: ResponseParserError.NotFound) {
            sender.sendRichMessage(
                "<red>Error: Code is invalid or expired</red>",
            )
        }
    }
}
