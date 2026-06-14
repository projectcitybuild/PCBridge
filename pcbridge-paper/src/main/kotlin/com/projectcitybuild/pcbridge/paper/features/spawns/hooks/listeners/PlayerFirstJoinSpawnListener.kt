package com.projectcitybuild.pcbridge.paper.features.spawns.hooks.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.paper.features.spawns.domain.repositories.SpawnRepository
import com.projectcitybuild.pcbridge.paper.features.spawns.spawnsTracer
import com.projectcitybuild.pcbridge.paper.l10n.l10n
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent

class PlayerFirstJoinSpawnListener(
    private val playerTeleporter: PlayerTeleporter,
    private val spawnRepository: SpawnRepository,
): Listener {
    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true,
    )
    suspend fun onPlayerJoin(
        event: PlayerJoinEvent,
    ) {
        val player = event.player

        // Check whether the server world has any data for this UUID.
        // No point changing their location if they have data already.
        if (player.hasPlayedBefore()) return

        return event.scoped(spawnsTracer, this::class.java) {
            val spawn = spawnRepository.get(player.location.world)

            log.info { "Moving first time joiner (${player.uniqueId}) to spawn ($spawn)" }

            playerTeleporter.move(
                player = player,
                destination = spawn,
                cause = PlayerTeleportEvent.TeleportCause.COMMAND,
            )
        }
    }
}