package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.projectcitybuild.pcbridge.features.worlds.WorldsPermissions
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderShape
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import org.bukkit.plugin.Plugin

class WorldBorderCommand(
    private val plugin: Plugin,
    private val worldBorderRepository: WorldBorderRepository,
    private val borderSetCommand: BorderSetCommand,
    private val fillCommand: BorderFillCommand,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val builder =
        command("border") {
            description = "Sets and enforces a world's border"
            requiresPermission(WorldsPermissions.MANAGE)
            executesSuspending(taskExecutor, ::show)
            then(borderSetCommand)
            then(fillCommand)
        }

    private suspend fun show(context: PaperCommandContext) {
        val player = context.source.requirePlayer()
        val border = worldBorderRepository.get(player.world)
        if (border == null) {
            player.sendMessage("No world border is configured for this world.")
            return
        }
        val shape = if (border.effectiveShape == WorldBorderShape.CIRCLE) {
            "circular"
        } else {
            "square"
        }
        player.sendMessage(
            "World border: $shape, center ${border.centerX}, ${border.centerZ}; radius ${border.radius} blocks.",
        )
    }
}
