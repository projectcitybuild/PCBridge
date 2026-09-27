package com.projectcitybuild.pcbridge.paper.features.spawns.hooks.commands

import com.projectcitybuild.pcbridge.paper.core.libs.permissions.PermissionNode
import com.projectcitybuild.pcbridge.paper.features.spawns.domain.data.SpawnUpdatedEvent
import com.projectcitybuild.pcbridge.paper.features.spawns.domain.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.paper.features.spawns.spawnsTracer
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.platform.paper.commands.requiresPermission
import com.projectcitybuild.pcbridge.paper.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.runtime.commands.scoped
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class SetSpawnCommand(
    private val plugin: Plugin,
    private val spawnRepository: SpawnRepository,
    private val eventBroadcaster: SpigotEventBroadcaster,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("setspawn")
            .requiresPermission(PermissionNode.SPAWN_MANAGE)
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(context: PaperCommandContext) =
        context.scoped(spawnsTracer) {
            val player = context.source.requirePlayer()
            val location = player.location

            spawnRepository.set(location)

            eventBroadcaster.broadcast(
                SpawnUpdatedEvent(location.world.uid, location),
            )
            player.sendRichMessage(l10n.spawnSet(location))
        }
}
