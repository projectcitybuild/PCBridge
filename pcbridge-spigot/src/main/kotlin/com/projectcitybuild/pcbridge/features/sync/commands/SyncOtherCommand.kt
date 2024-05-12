package com.projectcitybuild.pcbridge.features.sync.commands

import com.projectcitybuild.pcbridge.features.sync.actions.UpdatePlayerGroups
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

class SyncOtherCommand(
    private val server: Server,
    private val updatePlayerGroups: UpdatePlayerGroups,
) : SpigotCommand<SyncOtherCommand.Args> {
    override val label = "syncother"

    override val usage = CommandHelpBuilder()

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        val targetPlayer = server.getPlayer(args.targetPlayerName)
        checkNotNull(targetPlayer) {
            "Player ${args.targetPlayerName} not found"
        }
        val result = updatePlayerGroups.execute(targetPlayer.uniqueId)
        when (result) {
            is Failure ->
                when (result.reason) {
                    UpdatePlayerGroups.FailureReason.ACCOUNT_NOT_LINKED,
                    ->
                        sender.sendMessage(
                            Component.text("Error: Player does not have a linked PCB account")
                                .color(NamedTextColor.RED),
                        )
                }
            is Success -> {
                sender.sendMessage(
                    Component.text("${targetPlayer.name} has been synchronized")
                        .color(NamedTextColor.GRAY)
                        .decorate(TextDecoration.ITALIC),
                )
                targetPlayer.sendMessage(
                    Component.text("Your rank has been synchronized")
                        .color(NamedTextColor.GRAY)
                        .decorate(TextDecoration.ITALIC),
                )
            }
        }
    }

    data class Args(
        val targetPlayerName: String,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty() || args.size > 1) {
                    throw BadCommandUsageException()
                }
                return Args(targetPlayerName = args[0])
            }
        }
    }
}
