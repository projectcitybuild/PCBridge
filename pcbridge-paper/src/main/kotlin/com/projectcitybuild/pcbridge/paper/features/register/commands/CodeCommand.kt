package com.projectcitybuild.pcbridge.paper.features.register.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.services.pcb.RegisterHttpService
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
            sender.sendMessage(
                Component.text("Registration complete! Your account will be synced momentarily...")
                    .color(NamedTextColor.GREEN),
            )
        } catch (e: ResponseParser.NotFoundError) {
            sender.sendMessage(
                Component.text("Error: Code is invalid or expired")
                    .color(NamedTextColor.RED),
            )
        }
    }
}
