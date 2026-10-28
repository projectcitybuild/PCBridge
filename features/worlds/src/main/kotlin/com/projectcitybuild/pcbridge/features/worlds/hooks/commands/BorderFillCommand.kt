package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.worlds.WorldsPermissions
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldBorderFillService
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import org.bukkit.plugin.Plugin

class BorderFillCommand(
    plugin: Plugin,
    private val fillService: WorldBorderFillService,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    constructor(
        plugin: Plugin,
        worldBorderRepository: WorldBorderRepository,
        taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
        configuredChunkLimit: Int? = null,
    ) : this(plugin, WorldBorderFillService(worldBorderRepository, configuredChunkLimit), taskExecutor)

    override val builder = command("fill") {
        description = l10n.worlds.borderFillCommandDescription
        requiresPermission(WorldsPermissions.MANAGE)
        executesSuspending(taskExecutor) { fillService.preview(it.source.requirePlayer()) }
        literal("confirm") {
            executesSuspending(taskExecutor) { fillService.confirm(it.source.requirePlayer()) }
        }
        literal("status") {
            executesSuspending(taskExecutor) { fillService.status(it.source.requirePlayer()) }
        }
        literal("cancel") {
            executesSuspending(taskExecutor) { fillService.cancel(it.source.requirePlayer()) }
        }
    }

    companion object {
        const val DEFAULT_CHUNK_LIMIT = WorldBorderFillService.DEFAULT_CHUNK_LIMIT
        const val HARD_MAX_CHUNKS_PER_FILL = WorldBorderFillService.HARD_MAX_CHUNKS_PER_FILL
    }
}
