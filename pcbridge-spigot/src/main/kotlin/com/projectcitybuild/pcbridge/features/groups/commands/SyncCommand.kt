package com.projectcitybuild.pcbridge.features.groups.commands

import com.projectcitybuild.pcbridge.features.groups.events.PlayerSyncRequestedEvent
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.support.spigot.SpigotEventBroadcaster
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SyncCommand(
    private val server: Server,
    private val eventBroadcaster: SpigotEventBroadcaster,
) : SpigotCommand<SyncCommand.Args> {
    override val label = "sync"

    override val usage = CommandHelpBuilder()

    override suspend fun run(
        sender: CommandSender,
        args: Args,
    ) {
        check(sender is Player) {
            "Only players can use this command"
        }
        if (args.playerName.isEmpty()) {
            sync(sender, message = "Fetching player data...")
        } else {
            syncOther(sender, playerName = args.playerName)
        }
    }

    private suspend fun syncOther(sender: Player, playerName: String) {
        check (sender.hasPermission("pcbridge.sync.other")) {
            "You do not have permission"
        }
        val player = server.onlinePlayers.firstOrNull { it.name.lowercase() == playerName.lowercase() }
        if (player == null) {
            sender.sendMessage(
                MiniMessage.miniMessage().deserialize("<color:red>Player not found: ${playerName}</color>")
            )
            return
        }
        sync(player, message = "Fetching player data for ${player.name}...")

        sender.sendMessage(
            MiniMessage.miniMessage().deserialize("<color:green>Player data synced</color>")
        )
    }

    private suspend fun sync(player: Player, message: String) {
        player.sendMessage(
            MiniMessage.miniMessage().deserialize("<color:gray>$message</color>")
        )
        eventBroadcaster.broadcast(
            PlayerSyncRequestedEvent(playerUUID = player.uniqueId),
        )
    }

    data class Args(
        val playerName: String,
    ) {
        class Parser : CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    return Args(playerName = "")
                }
                if (args.size > 1) {
                    throw BadCommandUsageException()
                }
                return Args(playerName = args[0])
            }
        }
    }
}
