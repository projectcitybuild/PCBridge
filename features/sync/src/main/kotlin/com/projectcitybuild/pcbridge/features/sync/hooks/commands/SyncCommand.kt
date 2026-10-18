package com.projectcitybuild.pcbridge.features.sync.hooks.commands

import com.projectcitybuild.pcbridge.features.sync.SyncPermissions
import com.projectcitybuild.pcbridge.features.sync.domain.actions.SyncPlayer
import com.projectcitybuild.pcbridge.features.sync.syncTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.argumenttypes.SingleOnlinePlayerArgument
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class SyncCommand(
    private val plugin: Plugin,
    private val syncPlayer: SyncPlayer,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override fun literal(): PaperCommandNode = Commands
        .literal("sync")
        .then(
            Commands
                .argument("player", SingleOnlinePlayerArgument(plugin.server))
                .requiresPermission(SyncPermissions.OTHER)
                .executesSuspending(taskExecutor, ::syncOther),
        ).executesSuspending(taskExecutor, ::syncSelf)
        .build()

    private suspend fun syncSelf(context: PaperCommandContext) = context.scoped(syncTracer) {
        val player = context.source.requirePlayer()

        player.sendRichMessage("<gray>Fetching player data...</gray>")
        syncPlayer.execute(playerUUID = player.uniqueId)
    }

    private suspend fun syncOther(context: PaperCommandContext) = context.scoped(syncTracer) {
        val sender = context.source.sender
        val player = context.getArgument("player", Player::class.java)

        if (sender is Player && sender == player) {
            syncSelf(context)
            return@scoped
        }
        sender.sendRichMessage("<gray>Fetching player data for ${player.name}...</gray>")
        syncPlayer.execute(playerUUID = player.uniqueId)
        sender.sendRichMessage("<green>Player data synced</green>")
    }
}
