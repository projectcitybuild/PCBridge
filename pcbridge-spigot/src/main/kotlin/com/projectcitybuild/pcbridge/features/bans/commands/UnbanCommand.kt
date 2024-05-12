package com.projectcitybuild.pcbridge.features.bans.commands

import com.projectcitybuild.pcbridge.features.bans.actions.UnbanUUID
import com.projectcitybuild.pcbridge.utils.Failure
import com.projectcitybuild.pcbridge.utils.Success
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class UnbanCommand(
    private val unbanUUID: UnbanUUID,
): SpigotCommand<UnbanCommand.Args> {
    override val label = "unban"

    override val usage = CommandHelpBuilder() // TODO

    override suspend fun run(sender: CommandSender, args: Args) {
        val result = unbanUUID.unban(
            targetPlayerName= args.targetPlayerName,
            unbannerUUID = if (sender is Player) sender.uniqueId else null,
        )
        val message = when (result) {
            is Failure -> Component.text(result.reason.toMessage(args.targetPlayerName))
                .color(NamedTextColor.RED)
            is Success -> Component.text("{${args.targetPlayerName} has been unbanned")
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC)
        }
        sender.sendMessage(message)
    }

    data class Args(
        val targetPlayerName: String,
    ) {
        class Parser: CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                return Args(targetPlayerName = args[0])
            }
        }
    }
}

private fun UnbanUUID.FailureReason.toMessage(targetPlayerName: String) = when (this) {
    UnbanUUID.FailureReason.PlayerDoesNotExist -> "Could not find UUID for $targetPlayerName. This player likely doesn't exist"
    UnbanUUID.FailureReason.PlayerNotBanned -> "$targetPlayerName is not currently banned"
}