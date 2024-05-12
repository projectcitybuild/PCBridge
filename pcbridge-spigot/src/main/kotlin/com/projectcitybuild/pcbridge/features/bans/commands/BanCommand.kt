package com.projectcitybuild.pcbridge.features.bans.commands

import com.projectcitybuild.pcbridge.features.bans.actions.BanUUID
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.utils.Failure
import com.projectcitybuild.pcbridge.utils.Success
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BanCommand(
    private val banUUID: BanUUID,
    private val server: Server,
) : SpigotCommand<BanCommand.Args> {
    override val label = "ban"

    override val usage = CommandHelpBuilder()

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        val result =
            banUUID.ban(
                targetPlayerName = args.targetPlayerName,
                bannerUUID = if (sender is Player) sender.uniqueId else null,
                bannerName = sender.name,
                reason = args.reason,
            )
        when (result) {
            is Failure ->
                sender.sendMessage(
                    Component.text(result.reason.toMessage(args.targetPlayerName))
                        .color(NamedTextColor.RED),
                )
            is Success ->
                server.broadcast(
                    Component.text("${args.targetPlayerName} has been banned")
                        .color(NamedTextColor.GRAY)
                        .decorate(TextDecoration.ITALIC),
                )
        }
    }

    data class Args(
        val targetPlayerName: String,
        val reason: String,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                return Args(
                    targetPlayerName = args[0],
                    reason = if (args.size > 1) args.drop(1).joinToString(separator = " ") else "",
                )
            }
        }
    }
}

private fun BanUUID.FailureReason.toMessage(targetPlayerName: String) =
    when (this) {
        BanUUID.FailureReason.PlayerDoesNotExist,
        -> "Error: Could not find UUID for $targetPlayerName. This player likely doesn't exist"

        BanUUID.FailureReason.PlayerAlreadyBanned,
        -> "Error: $targetPlayerName is already banned"
    }
