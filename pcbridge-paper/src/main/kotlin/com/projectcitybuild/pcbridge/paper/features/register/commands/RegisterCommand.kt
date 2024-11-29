package com.projectcitybuild.pcbridge.paper.features.register.commands

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.services.pcb.RegisterHttpService
import com.projectcitybuild.pcbridge.paper.core.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.paper.core.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.paper.core.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class RegisterCommand(
    private val registerHttpService: RegisterHttpService,
) : SpigotCommand<RegisterCommand.Args> {
    override val label = "register"

    override val usage = CommandHelpBuilder(usage = "/register <email>")

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        check(sender is Player) {
            "Only players can use this command"
        }
        try {
            registerHttpService.sendCode(
                email = args.email,
                playerAlias = sender.name,
                playerUUID = sender.uniqueId,
            )
            sender.sendMessage(
                MiniMessage.miniMessage().deserialize(
                    "<color:gray>A code has been emailed to ${args.email}.<newline>" +
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

    data class Args(
        val email: String,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                if (args.size > 1) {
                    throw BadCommandUsageException()
                }
                return Args(email = args[0])
            }
        }
    }
}
