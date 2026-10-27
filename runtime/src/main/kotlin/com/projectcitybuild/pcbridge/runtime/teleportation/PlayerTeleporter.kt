package com.projectcitybuild.pcbridge.runtime.teleportation

import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.runtime.teleportation.exceptions.SafeDestinationNotFoundException
import com.projectcitybuild.pcbridge.runtime.teleportation.exceptions.TeleportDestinationRejectedException
import com.projectcitybuild.pcbridge.runtime.teleportation.exceptions.TeleportFailedException
import com.projectcitybuild.pcbridge.runtime.teleportation.storage.TeleportHistoryStorage
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import kotlin.coroutines.CoroutineContext

class PlayerTeleporter(
    private val safeYLocationFinder: SafeYLocationFinder,
    private val teleportHistoryStorage: TeleportHistoryStorage,
    private val server: Server,
    private val minecraftDispatcher: CoroutineContext,
) {
    data class TeleportOptions(
        val preloadDestinationChunk: Boolean = true,
        val adjustYForSafety: Boolean = false,
        val snapToBlockCenter: Boolean = false,
        val playSound: Boolean = true,
        val recordHistory: Boolean = true,
    )

    suspend fun move(
        player: Player,
        destination: Location,
        cause: TeleportCause = TeleportCause.PLUGIN,
        options: TeleportOptions = TeleportOptions(),
    ) = moveValidated(player, destination, cause, options) { true }

    suspend fun moveValidated(
        player: Player,
        destination: Location,
        cause: TeleportCause = TeleportCause.PLUGIN,
        options: TeleportOptions = TeleportOptions(),
        validateDestination: (Location) -> Boolean,
    ) = teleportationTracer.trace("PlayerTeleporter.move") {
        val currentLocation = withContext(minecraftDispatcher) {
            if (!player.isOnline) {
                throw TeleportFailedException("Cannot teleport an offline player")
            }
            player.location.clone()
        }

        if (options.preloadDestinationChunk) {
            withContext(minecraftDispatcher) {
                if (server.getWorld(destination.world.uid) !== destination.world) {
                    throw TeleportFailedException("Destination world is unavailable")
                }
                destination.world.getChunkAtAsyncUrgently(destination)
            }.await()
        }
        val teleportLocation = withContext(minecraftDispatcher) {
            if (!player.isOnline) {
                throw TeleportFailedException("Cannot teleport an offline player")
            }
            if (options.preloadDestinationChunk) {
                if (server.getWorld(destination.world.uid) !== destination.world ||
                    !destination.world.isChunkLoaded(destination.blockX shr 4, destination.blockZ shr 4)
                ) {
                    throw TeleportFailedException("Destination chunk is unavailable")
                }
            }

            adjustedDestination(destination, options).also { safeDestination ->
                if (!validateDestination(safeDestination)) {
                    throw TeleportDestinationRejectedException("Teleport destination is no longer valid")
                }
            }
        }
        val teleportFuture = withContext(minecraftDispatcher) {
            player.teleportAsync(teleportLocation, cause)
        }
        val success = teleportFuture.await()
        if (!success) {
            log.warn { "Teleport failed ($teleportLocation)" }
            throw TeleportFailedException("Failed to teleport to $destination")
        }

        withContext(minecraftDispatcher) {
            if (options.playSound) {
                val volume = 1F
                val pitch = 1F
                player.world.playSound(destination, Sound.ENTITY_PLAYER_TELEPORT, volume, pitch)
            }

            if (options.recordHistory) {
                teleportHistoryStorage.put(
                    prev = currentLocation,
                    player = player,
                )
            }
        }
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
                x = blockX,
                z = blockZ,
            )?.toDouble() ?: throw SafeDestinationNotFoundException("Safe destination could not be found")
    }
}
