package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.worlds.WorldsPermissions
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldBorderService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import org.bukkit.plugin.Plugin

class BorderClearCommand(
    plugin: Plugin,
    private val worldBorderService: WorldBorderService,
    taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val builder = command("clear") {
        description = l10n.worlds.borderClearCommandDescription
        requiresPermission(WorldsPermissions.MANAGE)
        executesSuspending(taskExecutor) { context ->
            val player = context.source.requirePlayer()
            worldBorderService.clear(player.world)
            player.sendMessage(l10n.worlds.borderCleared)
        }
    }
}
