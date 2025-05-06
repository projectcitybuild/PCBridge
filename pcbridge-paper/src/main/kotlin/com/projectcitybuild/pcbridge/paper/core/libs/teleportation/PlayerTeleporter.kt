package com.projectcitybuild.pcbridge.paper.core.libs.teleportation

import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.exceptions.SafeDestinationNotFoundException
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.exceptions.TeleportFailedException
import com.projectcitybuild.pcbridge.paper.core.libs.teleportation.storage.TeleportHistoryStorage
import kotlinx.coroutines.future.await
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause

class PlayerTeleporter(
    private val safeYLocationFinder: SafeYLocationFinder,
    private val teleportHistoryStorage: TeleportHistoryStorage,
) {
    data class TeleportOptions(
        val cause: TeleportCause = TeleportCause.PLUGIN,
        val preloadDestinationChunks: Boolean = false,
        val adjustYForSafety: Boolean = false,
        val snapToBlockCenter: Boolean = false,
    )

    suspend fun move(
        player: Player,
        destination: Location,
        options: TeleportOptions = TeleportOptions(),
    ) {
        val currentLocation = player.location.clone()

        if (options.preloadDestinationChunks) {
            destination.world.getChunkAtAsyncUrgently(destination).await()
        }
        val teleportLocation = adjustedDestination(destination, options)

        val success = player.teleportAsync(teleportLocation, options.cause).await()
        if (!success) {
           log.warn { "Teleport failed ($teleportLocation)" }
           throw TeleportFailedException("Failed to teleport to destination")
        }

        teleportHistoryStorage.put(
            prev = currentLocation,
            next = teleportLocation,
            player = player,
        )
    }

    private fun adjustedDestination(destination: Location, options: TeleportOptions): Location {
        var location = destination.clone()
        if (options.adjustYForSafety) {
            location = safeLocation(location)
        }
        if (options.snapToBlockCenter) {
            location = location.toCenterLocation()
        }
        return location
    }

    private fun safeLocation(initial: Location): Location {
        return initial.apply {
            y = safeYLocationFinder.findY(
                world = world,
                x = x.toInt(),
                z = z.toInt(),
            )?.toDouble() ?: throw SafeDestinationNotFoundException("Safe destination could not be found")
        }
    }
}