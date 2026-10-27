package com.projectcitybuild.pcbridge.features.worlds.domain.services

import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderLimits
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderUpdatedEvent
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.runtime.teleportation.exceptions.TeleportDestinationRejectedException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.coroutines.CoroutineContext

class WorldBorderService(
    private val server: Server,
    private val repository: WorldBorderRepository,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val playerTeleporter: PlayerTeleporter,
    private val minecraftDispatcher: CoroutineContext,
) {
    fun load(world: World): WorldBorder? {
        val border = repository.get(world)
        if (border == null) {
            world.restoreDefaultNativeBorder()
        } else {
            world.applyNativeBorder(border, validatedNativeBorderSize(border))
        }
        return border
    }

    fun loadAllLoaded() {
        server.worlds.forEach(::load)
    }

    suspend fun set(
        world: World,
        border: WorldBorder,
    ): List<UUID> {
        val nativeSize = validatedNativeBorderSize(border)
        val nativeSnapshot = withContext(minecraftDispatcher) { NativeBorderSnapshot.capture(world) }

        withContext(NonCancellable) {
            try {
                withContext(minecraftDispatcher) {
                    world.applyNativeBorder(border, nativeSize)
                }
            } catch (failure: Throwable) {
                restoreNativeBorder(world, nativeSnapshot, failure)
                throw failure
            }

            try {
                repository.set(world, border)
            } catch (failure: Throwable) {
                restoreNativeBorder(world, nativeSnapshot, failure)
                throw failure
            }
        }

        val failures = mutableListOf<UUID>()
        var operationFailure: Throwable? = null
        try {
            val affectedPlayers = withContext(minecraftDispatcher) {
                server.onlinePlayers.filter { player ->
                    player.world.uid == world.uid
                }
            }
            affectedPlayers.forEach { player ->
                try {
                    relocateIfOutside(player, world)
                } catch (cancelled: CancellationException) {
                    currentCoroutineContext().ensureActive()
                    reportRelocationFailure(player, world, cancelled, failures)
                } catch (failure: Exception) {
                    reportRelocationFailure(player, world, failure, failures)
                }
            }
        } catch (failure: Throwable) {
            operationFailure = failure
        }

        broadcastCommittedUpdate(world, border, operationFailure)
        operationFailure?.let { throw it }
        return failures
    }

    suspend fun relocateIfOutside(player: Player) {
        val world = withContext(minecraftDispatcher) { player.world }
        relocateIfOutside(player, world)
    }

    suspend fun clear(world: World) {
        val nativeSnapshot = withContext(minecraftDispatcher) { NativeBorderSnapshot.capture(world) }

        withContext(NonCancellable) {
            try {
                withContext(minecraftDispatcher) {
                    world.worldBorder.setCenter(0.0, 0.0)
                    world.worldBorder.size = WorldBorderLimits.MAX_DIMENSION
                }
                repository.clear(world)
            } catch (failure: Throwable) {
                restoreNativeBorder(world, nativeSnapshot, failure)
                throw failure
            }
        }

        withContext(NonCancellable) {
            eventBroadcaster.broadcast(WorldBorderUpdatedEvent(world, null))
        }
    }

    private fun validatedNativeBorderSize(border: WorldBorder): Double {
        val geometry = border.geometry
        val size = geometry.nativeBorderSize
        require(size.isFinite() && size > 0.0 && size <= WorldBorderLimits.MAX_DIMENSION) {
            "World border dimensions are invalid"
        }
        require(geometry.contains(border.centerX, border.centerZ)) {
            "World border center is invalid"
        }
        return size
    }

    private suspend fun restoreNativeBorder(
        world: World,
        snapshot: NativeBorderSnapshot,
        failure: Throwable,
    ) {
        try {
            withContext(minecraftDispatcher) {
                world.worldBorder.setCenter(snapshot.centerX, snapshot.centerZ)
            }
        } catch (restoreFailure: Throwable) {
            failure.addSuppressedIfDistinct(restoreFailure)
        }
        try {
            withContext(minecraftDispatcher) {
                world.worldBorder.size = snapshot.size
            }
        } catch (restoreFailure: Throwable) {
            failure.addSuppressedIfDistinct(restoreFailure)
        }
    }

    private suspend fun broadcastCommittedUpdate(
        world: World,
        border: WorldBorder,
        operationFailure: Throwable?,
    ) {
        try {
            withContext(NonCancellable) {
                eventBroadcaster.broadcast(WorldBorderUpdatedEvent(world, border))
            }
        } catch (broadcastFailure: Throwable) {
            if (operationFailure == null) throw broadcastFailure
            operationFailure.addSuppressedIfDistinct(broadcastFailure)
        }
    }

    private suspend fun reportRelocationFailure(
        player: Player,
        world: World,
        failure: Throwable,
        failures: MutableList<UUID>,
    ) {
        failures += player.uniqueId
        log.error(failure) {
            "Failed to relocate ${player.name} (${player.uniqueId}) after world border update for ${world.name}"
        }
    }

    private suspend fun relocateIfOutside(
        player: Player,
        world: World,
    ) = withContext(minecraftDispatcher) {
        repeat(MAX_RELOCATION_ATTEMPTS) {
            if (!player.isAvailableIn(world)) return@withContext
            val border = repository.get(world) ?: return@withContext
            val currentLocation = player.location
            if (border.geometry.contains(currentLocation.x, currentLocation.z)) return@withContext

            val (x, z) = border.geometry.clamp(currentLocation.x, currentLocation.z)
            val destination = currentLocation.clone().apply {
                this.x = x
                this.z = z
            }
            try {
                playerTeleporter.moveValidated(
                    player = player,
                    destination = destination,
                    options = PlayerTeleporter.TeleportOptions(
                        adjustYForSafety = true,
                        playSound = false,
                        recordHistory = false,
                    ),
                    validateDestination = { candidate ->
                        player.isAvailableIn(world) &&
                            repository.get(world)?.geometry?.contains(candidate.x, candidate.z) == true
                    },
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: TeleportDestinationRejectedException) {
                if (!player.isAvailableIn(world)) return@withContext
                return@repeat
            } catch (failure: Exception) {
                if (!player.isAvailableIn(world)) return@withContext
                throw failure
            }

            if (!player.isAvailableIn(world)) return@withContext
            val currentBorder = repository.get(world) ?: return@withContext
            val relocated = player.location
            if (currentBorder.geometry.contains(relocated.x, relocated.z)) return@withContext
        }

        throw TeleportDestinationRejectedException(
            "World border changed repeatedly while relocating ${player.uniqueId} in ${world.name}",
        )
    }

    private fun Player.isAvailableIn(world: World): Boolean =
        isOnline && this.world.uid == world.uid && this@WorldBorderService.server.getWorld(world.uid) === world

    private fun World.applyNativeBorder(
        border: WorldBorder,
        nativeSize: Double,
    ) {
        worldBorder.setCenter(border.centerX, border.centerZ)
        worldBorder.size = nativeSize
    }

    private fun World.restoreDefaultNativeBorder() {
        worldBorder.setCenter(0.0, 0.0)
        worldBorder.size = WorldBorderLimits.MAX_DIMENSION
    }

    private companion object {
        const val MAX_RELOCATION_ATTEMPTS = 3
    }

    private data class NativeBorderSnapshot(
        val centerX: Double,
        val centerZ: Double,
        val size: Double,
    ) {
        companion object {
            fun capture(world: World): NativeBorderSnapshot {
                val border = world.worldBorder
                val center = border.center
                return NativeBorderSnapshot(center.x, center.z, border.size)
            }
        }
    }

    private fun Throwable.addSuppressedIfDistinct(other: Throwable) {
        if (this !== other) addSuppressed(other)
    }
}
