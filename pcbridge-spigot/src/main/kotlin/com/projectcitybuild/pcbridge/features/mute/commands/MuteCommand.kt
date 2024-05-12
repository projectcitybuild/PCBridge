package com.projectcitybuild.pcbridge.features.mute.commands

import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import io.github.reactivecircus.cache4k.Cache
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Server
import org.bukkit.command.CommandSender
import java.util.UUID

class MuteCommand(
    private val server: Server,
    private val mutedPlayers: Cache<UUID, Unit>,
): SpigotCommand<MuteCommand.Args> {
    override val label = "mute"

    override val usage = CommandHelpBuilder() // TODO

    override suspend fun run(sender: CommandSender, args: Args) {
        val player = server.getPlayer(args.playerName)
        checkNotNull (player) {
            "Player ${args.playerName} not found"
        }
        check (mutedPlayers.get(player.uniqueId) == null) {
            "Player ${args.playerName} is already muted"
        }
        mutedPlayers.put(player.uniqueId, Unit)

        sender.sendMessage(
            Component.text("Player ${args.playerName} has been muted")
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC)
        )

        player.sendMessage(
            Component.text("You have been muted")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.ITALIC)
        )
    }

    data class Args(
        val playerName: String,
    ) {
        class Parser: CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                return Args(playerName = args[0])
            }
        }
    }
}