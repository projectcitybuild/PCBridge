package com.projectcitybuild.pcbridge.paper.features.register.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.http.services.pcb.RegisterHttpService
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
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
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = traceCommand(context) {
        val email = context.getArgument("email", String::class.java)
        val sender = context.source.sender
        check(sender is Player) { "Only players can use this command" }

        registerHttpService.sendCode(
            email = email,
            playerAlias = sender.name,
            playerUUID = sender.uniqueId,
        )
        sender.sendMessage(
            MiniMessage.miniMessage().deserialize(
                "<gray>A code has been emailed to $email.<newline>" +
                    "Please type it in with <aqua><bold><hover:show_text:'/code'><click:suggest_command:/code >/code [code]</click></hover></bold></aqua></gray>"
            )
        )
    }
}
