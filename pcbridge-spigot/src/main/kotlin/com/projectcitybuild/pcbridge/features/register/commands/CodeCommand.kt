package com.projectcitybuild.pcbridge.features.register.commands

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.services.pcb.RegisterHttpService
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CodeCommand(
    private val registerHttpService: RegisterHttpService,
) : SpigotCommand<CodeCommand.Args> {
    override val label = "code"

    override val usage = CommandHelpBuilder()

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        check(sender is Player) {
            "Only players can use this command"
        }
        try {
            registerHttpService.verifyCode(
                code = args.code,
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
        } catch (e: ResponseParser.ValidationError) {
            sender.sendMessage(
                Component.text("Error: ${e.message ?: "Unknown error occurred"}")
                    .color(NamedTextColor.RED),
            )
        }
    }

    data class Args(
        val code: String,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                if (args.size > 1) {
                    throw BadCommandUsageException()
                }
                return Args(code = args[0])
            }
        }
    }
}
