package com.projectcitybuild.pcbridge.paper.features.nicknames.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.arguments.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.traceSuspending
import com.projectcitybuild.pcbridge.paper.features.sync.actions.SyncPlayer
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class NicknameCommand(
    private val plugin: Plugin,
    private val syncPlayer: SyncPlayer,
) : BrigadierCommand {
    override fun buildLiteral(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("nickname")
            .then(
                Commands.argument("player", SingleOnlinePlayerArgument(plugin.server))
                    .requiresPermission(PermissionNode.PLAYER_SYNC_OTHER)
                    .executesSuspending(plugin, ::syncOther)
            )
            .executesSuspending(plugin, ::syncSelf)
            .build()
    }

    private suspend fun syncSelf(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val player = context.source.requirePlayer()

        player.sendRichMessage("<gray>Fetching player data...</gray>")
        syncPlayer.execute(playerUUID = player.uniqueId)
    }

    private suspend fun syncOther(context: CommandContext<CommandSourceStack>) = context.traceSuspending {
        val sender = context.source.sender
        val player = context.getArgument("player", Player::class.java)

        if (sender is Player && sender.equals(player)) {
            syncSelf(context)
            return@traceSuspending
        }
        sender.sendRichMessage("<gray>Fetching player data for ${player.name}...</gray>")
        syncPlayer.execute(playerUUID = player.uniqueId)
        sender.sendRichMessage("<green>Player data synced</green>")
    }
}
