package com.projectcitybuild.pcbridge.features.spawns.hooks.commands

import com.projectcitybuild.pcbridge.features.spawns.domain.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.features.spawns.spawnsTracer
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.PaperCommandNode
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.support.brigadier.extensions.requirePlayer
import com.projectcitybuild.pcbridge.runtime.commands.scoped
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

class SpawnCommand(
    private val plugin: Plugin,
    private val spawnRepository: SpawnRepository,
    private val playerTeleporter: PlayerTeleporter,
) : BrigadierCommand {
    override fun literal(): PaperCommandNode {
        return Commands.literal("spawn")
            .executesSuspending(plugin, ::execute)
            .build()
    }

    suspend fun execute(context: PaperCommandContext) =
        context.scoped(spawnsTracer) {
            val player = context.source.requirePlayer()

            val spawn = spawnRepository.get(player.location.world)

            playerTeleporter.move(
                player = player,
                destination = spawn,
                cause = PlayerTeleportEvent.TeleportCause.COMMAND,
            )
            player.sendMessage(l10n.teleportedToSpawn)
        }
}
