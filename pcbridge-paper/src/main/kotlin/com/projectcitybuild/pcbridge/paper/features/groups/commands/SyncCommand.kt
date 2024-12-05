package com.projectcitybuild.pcbridge.paper.features.groups.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.SinglePlayerArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.getOptionalArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.groups.events.PlayerSyncRequestedEvent
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class SyncCommand(
    private val plugin: Plugin,
    private val eventBroadcaster: SpigotEventBroadcaster,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("sync")
            .then(
                Commands.argument("player", SinglePlayerArgument(plugin.server))
                    .requiresPermission(PermissionNode.PLAYER_SYNC_OTHER)
                    .executesSuspending(plugin, ::execute)
            )
            .executesSuspending(plugin, ::execute)
            .build()
    }

    private suspend fun execute(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val sender = context.source.sender
        check(sender is Player) { "Only players can use this command" }

        val player = context.getOptionalArgument("player", Player::class.java)

        if (player == null) {
            sync(sender, message = "Fetching player data...")
        } else {
            syncOther(sender, player = player)
        }
    }

    private suspend fun syncOther(sender: Player, player: Player) {
        sync(player, message = "Fetching player data for ${player.name}...")

        sender.sendMessage(
            MiniMessage.miniMessage().deserialize("<green>Player data synced</green>")
        )
    }

    private suspend fun sync(player: Player, message: String) {
        player.sendMessage(
            MiniMessage.miniMessage().deserialize("<gray>$message</gray>")
        )
        eventBroadcaster.broadcast(
            PlayerSyncRequestedEvent(playerUUID = player.uniqueId),
        )
    }
}
