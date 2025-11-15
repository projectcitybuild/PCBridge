package com.projectcitybuild.pcbridge.paper.features.register.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.projectcitybuild.pcbridge.http.pcb.services.RegisterHttpService
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParserError
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.CommandNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class CodeCommand(
    private val plugin: Plugin,
    private val registerHttpService: RegisterHttpService,
) : BrigadierCommand {
    override val description: String = "Finishes account registration by verifying a code"

    override fun buildLiteral(): CommandNode {
        return Commands.literal("code")
            .then(
                Commands.argument("code", StringArgumentType.string())
                    .executesSuspending(plugin, ::execute)
            )
            .executes { context ->
                context.source.sender.sendRichMessage(l10n.errorNoCodeSpecified)
                return@executes Command.SINGLE_SUCCESS
            }
            .build()
    }

    private suspend fun execute(context: CommandContext) = context.traceSuspending {
        val player = context.source.requirePlayer()
        val code = context.getArgument("code", String::class.java)

        try {
            registerHttpService.verifyCode(
                code = code,
                playerUUID = player.uniqueId,
            )
            player.sendRichMessage(
                "<green>Registration complete! Your account will be synced momentarily...</green>",
            )
        } catch (e: ResponseParserError.NotFound) {
            player.sendRichMessage(l10n.errorCodeInvalidOrExpired)
        }
    }
}
