package com.projectcitybuild.pcbridge.paper.features.spawns.commands

import com.projectcitybuild.pcbridge.paper.PermissionNode
import com.projectcitybuild.pcbridge.paper.architecture.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.extensions.requiresPermission
import com.projectcitybuild.pcbridge.paper.architecture.commands.scopedSuspending
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.paper.core.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.features.spawns.events.SpawnUpdatedEvent
import com.projectcitybuild.pcbridge.paper.features.spawns.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.Plugin

class SetSpawnCommand(
    private val plugin: Plugin,
    private val spawnRepository: SpawnRepository,
    private val eventBroadcaster: SpigotEventBroadcaster,
) : BrigadierCommand {
    override fun buildLiteral(): PaperCommandNode {
        return Commands.literal("setspawn")
            .requiresPermission(PermissionNode.SPAWN_MANAGE)
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(context: PaperCommandContext) = context.scopedSuspending {
        val player = context.source.requirePlayer()
        val location = player.location

        spawnRepository.set(location)

        eventBroadcaster.broadcast(
            SpawnUpdatedEvent(location.world.uid, location)
        )
        player.sendRichMessage(l10n.spawnSet(location))
    }
}