package com.projectcitybuild.pcbridge.paper.features.register.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.services.pcb.RegisterHttpService
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

@Suppress("UnstableApiUsage")
class RegisterCommand(
    private val plugin: Plugin,
    private val registerHttpService: RegisterHttpService,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("register")
            .then(
                Commands.argument("email", StringArgumentType.string())
                    .executesSuspending(plugin, ::execute)
            )
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = traceCommand(context) {
        val email = context.getArgument("email", String::class.java)
        val sender = context.source.sender
        check(sender is Player) { "Only players can use this command" }

        try {
            registerHttpService.sendCode(
                email = email,
                playerAlias = sender.name,
                playerUUID = sender.uniqueId,
            )
            sender.sendMessage(
                MiniMessage.miniMessage().deserialize(
                    "<color:gray>A code has been emailed to $email.<newline>" +
                        "Please type it in with </color><color:aqua><bold><hover:show_text:'/code'><click:suggest_command:/code >/code [code]</click></hover></bold></color>"
                )
            )
        } catch (e: ResponseParser.ValidationError) {
            sender.sendMessage(
                Component.text("Error: ${e.message ?: "Unknown error occurred"}")
                    .color(NamedTextColor.RED),
            )
        }
    }
}
