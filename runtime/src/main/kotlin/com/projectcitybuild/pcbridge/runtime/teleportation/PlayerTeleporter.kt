package com.projectcitybuild.pcbridge.runtime.teleportation

import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.runtime.teleportation.exceptions.SafeDestinationNotFoundException
import com.projectcitybuild.pcbridge.runtime.teleportation.exceptions.TeleportFailedException
import com.projectcitybuild.pcbridge.runtime.teleportation.storage.TeleportHistoryStorage
import kotlinx.coroutines.future.await
import net.kyori.adventure.sound.Sound
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause

class PlayerTeleporter(
    private val safeYLocationFinder: SafeYLocationFinder,
    private val teleportHistoryStorage: TeleportHistoryStorage,
) {
    data class TeleportOptions(
        val preloadDestinationChunk: Boolean = true,
        val adjustYForSafety: Boolean = false,
        val snapToBlockCenter: Boolean = false,
        val playSound: Boolean = true,
    )

    suspend fun move(
        player: Player,
        destination: Location,
        cause: TeleportCause = TeleportCause.PLUGIN,
        options: TeleportOptions = TeleportOptions(),
    ) = teleportationTracer.trace("PlayerTeleporter.move") {
        val currentLocation = player.location.clone()
        val teleportLocation = adjustedDestination(destination, options)

        if (options.preloadDestinationChunk) {
            destination.world.getChunkAtAsyncUrgently(teleportLocation).await()
        }
        val success = player.teleportAsync(teleportLocation, cause).await()
        if (!success) {
            log.warn { "Teleport failed ($teleportLocation)" }
            throw TeleportFailedException("Failed to teleport to $destination")
        }

        if (options.playSound) {
            val volume = 1F
            val pitch = 1F
            player.playSound(player, org.bukkit.Sound.ENTITY_PLAYER_TELEPORT, volume, pitch)
        }

        teleportHistoryStorage.put(
            prev = currentLocation,
            player = player,
        )
    }

    private fun adjustedDestination(
        destination: Location,
        options: TeleportOptions,
    ): Location {
        var location = destination.clone()
        if (options.adjustYForSafety) {
            location = safeLocation(location)
        }
        if (options.snapToBlockCenter) {
            location = location.toCenterLocation()
        }
        return location
    }

    private fun safeLocation(initial: Location): Location = initial.apply {
        y = safeYLocationFinder
            .findY(
                world = world,
                x = x.toInt(),
                z = z.toInt(),
            )?.toDouble() ?: throw SafeDestinationNotFoundException("Safe destination could not be found")
    }
}
