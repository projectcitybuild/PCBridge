package com.projectcitybuild.pcbridge.paper.features.randomteleport.actions

import com.projectcitybuild.pcbridge.paper.core.data.Coordinate2
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.exceptions.SafeDestinationNotFoundException
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.exceptions.TeleportFailedException
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent

class FindRandomLocation(
    private val playerTeleporter: PlayerTeleporter,
) {
    suspend fun teleport(player: Player, attempts: Int = 5): Location? {
        val world = player.location.world

        for (i in 1..attempts) {
            val coordinate2 = randomCoordinate2(world)
            val location = Location(
                world,
                coordinate2.x,
                0.0,
                coordinate2.z
            )
            try {
                playerTeleporter.move(
                    player,
                    location,
                    cause = PlayerTeleportEvent.TeleportCause.COMMAND,
                    options = PlayerTeleporter.TeleportOptions(
                        preloadDestinationChunk = true,
                        adjustYForSafety = true,
                        snapToBlockCenter = true,
                    ),
                )
                return location
            } catch (e: SafeDestinationNotFoundException) {
                continue
            } catch (e: TeleportFailedException) {
                continue
            }
        }
        return null
    }

    private fun randomCoordinate2(world: World): Coordinate2 {
        val bounds = world.worldBorder
        val center = bounds.center
        val size = bounds.size

        // If a WorldBorder is not set, the API still returns an enormous bounds
        // (60 mil in both axis) so we need to always clamp this
        val xRange = (center.x - size).coerceAtLeast(BORDER_MIN).toInt()..
            (center.x + size).coerceAtMost(BORDER_MAX).toInt()
        val zRange = (center.z - size).coerceAtLeast(BORDER_MIN).toInt()..
            (center.z + size).coerceAtMost(BORDER_MAX).toInt()

        return Coordinate2(
            x = xRange.random().toDouble(),
            z = zRange.random().toDouble(),
        )
    }

    private companion object {
        const val BORDER_MAX = 15_000.0
        const val BORDER_MIN = BORDER_MAX * -1
    }
}