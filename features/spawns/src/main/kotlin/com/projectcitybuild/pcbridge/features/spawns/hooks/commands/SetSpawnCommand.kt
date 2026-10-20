package com.projectcitybuild.pcbridge.features.spawns.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.spawns.SpawnsPermissions
import com.projectcitybuild.pcbridge.features.spawns.domain.data.SpawnUpdatedEvent
import com.projectcitybuild.pcbridge.features.spawns.domain.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.features.spawns.spawnsTracer
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import org.bukkit.plugin.Plugin

class SetSpawnCommand(
    private val plugin: Plugin,
    private val spawnRepository: SpawnRepository,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val commandTaskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    override val build = command("setspawn") {
        description = "Sets the server spawn location"
        requiresPermission(SpawnsPermissions.MANAGE)
        executesSuspending(commandTaskExecutor, ::execute)
    }

    suspend fun execute(context: PaperCommandContext) = context.scoped(spawnsTracer) {
        val player = context.source.requirePlayer()
        val location = player.location

        spawnRepository.set(location)

        eventBroadcaster.broadcast(
            SpawnUpdatedEvent(location.world.uid, location),
        )
        player.sendMessage(l10n.spawns.spawnSet(location))
    }
}
