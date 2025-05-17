package com.projectcitybuild.pcbridge.paper.features.register.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.http.pcb.services.RegisterHttpService
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class RegisterCommand(
    private val plugin: Plugin,
    private val registerHttpService: RegisterHttpService,
) : BrigadierCommand {
    override val description: String = "Creates a new Project City Build account"

    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("register")
            .then(
                Commands.argument("email", StringArgumentType.greedyString())
                    .executesSuspending(plugin, ::execute)
            )
            .executes { context ->
                context.source.sender.sendRichMessage(
                    "<red>Error: Please specify an email address to receive your registration code</red><newline><gray>Example Usage: <bold>/register your@email.com</bold></gray>",
                )
                return@executes Command.SINGLE_SUCCESS
            }
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val player = context.source.requirePlayer()
        val email = context.getArgument("email", String::class.java)

        registerHttpService.sendCode(
            email = email,
            playerAlias = player.name,
            playerUUID = player.uniqueId,
        )
        player.sendRichMessage(
            "<gray>A code has been emailed to $email.<newline>" +
            "Please type it in with <aqua><bold><hover:show_text:'/code'><click:suggest_command:/code >/code [code]</click></hover></bold></aqua></gray>"
        )
    }
}
