package com.projectcitybuild.pcbridge.features.worlds.domain.services

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class WorldBorderFillService(
    private val worldBorderRepository: WorldBorderRepository,
    configuredChunkLimit: Int? = null,
) {
    private data class PendingFill(
        val worldId: UUID,
        val border: WorldBorder,
        val totalChunks: Long,
        val expiresAt: TimeMark,
    )

    private data class ActiveFill(
        val ownerId: UUID,
        val job: Job,
        val totalChunks: Long,
        var checked: Long = 0,
        var generated: Long = 0,
    )

    private val maxChunksPerFill = configuredChunkLimit
        ?.takeIf { it > 0 }
        ?.toLong()
        ?.coerceAtMost(HARD_MAX_CHUNKS_PER_FILL)
        ?: DEFAULT_CHUNK_LIMIT.toLong()

    private val pendingFills = mutableMapOf<UUID, PendingFill>()
    private val activeFills = mutableMapOf<UUID, ActiveFill>()

    fun onPlayerQuit(playerId: UUID) {
        pendingFills.remove(playerId)
        activeFills.values
            .filter { it.ownerId == playerId }
            .forEach { it.job.cancel(CancellationException("Fill owner disconnected")) }
    }

    fun onWorldUnload(worldId: UUID) {
        pendingFills.entries.removeIf { it.value.worldId == worldId }
        activeFills[worldId]?.job?.cancel(CancellationException("Fill world unloaded"))
    }

    fun preview(player: Player) {
        pendingFills.entries.removeIf {
            it.value.expiresAt.hasPassedNow()
        }
        val border = worldBorderRepository.get(player.world)
            ?: run {
                player.sendMessage(l10n.worlds.fillRequiresBorder)
                return
            }
        val geometry = border.geometry
        val chunkCount = geometry
            .chunkCoordinates()
            .take(maxChunksPerFill.toInt() + 1)
            .count()
            .toLong()
        if (chunkCount > maxChunksPerFill) {
            player.sendMessage(l10n.worlds.fillTooLarge(maxChunksPerFill))
            return
        }

        pendingFills[player.uniqueId] = PendingFill(
            worldId = player.world.uid,
            border = border,
            totalChunks = chunkCount,
            expiresAt = TimeSource.Monotonic.markNow() + CONFIRMATION_WINDOW,
        )
        player.sendMessage(l10n.worlds.fillPreview(chunkCount, player.world.name))
    }

    suspend fun confirm(player: Player) {
        val pending = pendingFills.remove(player.uniqueId)
        if (pending == null || pending.expiresAt.hasPassedNow()) {
            player.sendMessage(l10n.worlds.fillPreviewMissing)
            return
        }
        if (pending.worldId != player.world.uid) {
            player.sendMessage(l10n.worlds.fillWrongWorld)
            return
        }
        if (worldBorderRepository.get(player.world) != pending.border) {
            player.sendMessage(l10n.worlds.fillBorderChanged)
            return
        }

        val world = player.world
        val activeFill = ActiveFill(
            ownerId = player.uniqueId,
            job = currentCoroutineContext()[Job]
                ?: error("Border fill must run inside a coroutine job"),
            totalChunks = pending.totalChunks,
        )
        if (activeFills.putIfAbsent(world.uid, activeFill) != null) {
            player.sendMessage(l10n.worlds.fillAlreadyRunning)
            return
        }

        try {
            player.sendMessage(l10n.worlds.fillStarted)

            for (chunk in pending.border.geometry.chunkCoordinates()) {
                if (!player.isOnline) return
                if (!hasCurrentBorder(world, pending.border)) return fillStopped(player)
                val existingChunk = world.getChunkAtAsync(chunk.x, chunk.z, false).await()
                activeFill.checked++
                if (!player.isOnline) return
                if (!hasCurrentBorder(world, pending.border)) return fillStopped(player)
                if (existingChunk == null) {
                    if (!hasCurrentBorder(world, pending.border)) return fillStopped(player)
                    world.getChunkAtAsync(chunk.x, chunk.z, true).await()
                    activeFill.generated++
                    if (!player.isOnline) return
                    if (!hasCurrentBorder(world, pending.border)) return fillStopped(player)
                }

                if (activeFill.checked % CHUNKS_PER_BATCH == 0L) {
                    player.sendMessage(
                        l10n.worlds.fillProgress(
                            activeFill.checked,
                            activeFill.totalChunks,
                            activeFill.generated,
                        ),
                    )
                    delay(BATCH_DELAY)
                }
            }
            player.sendMessage(
                l10n.worlds.fillComplete(
                    activeFill.checked,
                    activeFill.totalChunks,
                    activeFill.generated,
                ),
            )
        } catch (cancelled: CancellationException) {
            if (player.isOnline) {
                player.sendMessage(
                    l10n.worlds.fillCancelled(
                        activeFill.checked,
                        activeFill.totalChunks,
                        activeFill.generated,
                    ),
                )
            }
            throw cancelled
        } catch (failure: Exception) {
            log.error(failure) { "World border fill failed for ${world.name}" }
            if (player.isOnline) player.sendMessage(l10n.worlds.fillFailed)
            throw failure
        } finally {
            if (activeFills[world.uid] === activeFill) activeFills.remove(world.uid)
        }
    }

    fun status(player: Player) {
        val activeFill = activeFills[player.world.uid]
        if (activeFill == null) {
            player.sendMessage(l10n.worlds.fillNoActiveJob)
            return
        }
        val state = when {
            activeFill.job.isCancelled -> "cancelling"
            activeFill.job.isActive -> "running"
            else -> "finishing"
        }
        player.sendMessage(
            l10n.worlds.fillStatus(
                activeFill.checked,
                activeFill.totalChunks,
                activeFill.generated,
                state,
            ),
        )
    }

    fun cancel(player: Player) {
        val activeFill = activeFills[player.world.uid]
        if (activeFill == null) {
            player.sendMessage(l10n.worlds.fillNoActiveJob)
            return
        }
        activeFill.job.cancel(CancellationException("Cancelled by ${player.name}"))
        player.sendMessage(l10n.worlds.fillCancelRequested)
    }

    private fun hasCurrentBorder(
        world: World,
        border: WorldBorder,
    ): Boolean = worldBorderRepository.get(world) == border

    private fun fillStopped(player: Player) {
        if (player.isOnline) player.sendMessage(l10n.worlds.fillStoppedBorderChanged)
    }

    companion object {
        const val DEFAULT_CHUNK_LIMIT = 4_096
        const val HARD_MAX_CHUNKS_PER_FILL = 50_000L
        private const val CHUNKS_PER_BATCH = 256L
        private val BATCH_DELAY: Duration = 1.milliseconds
        private val CONFIRMATION_WINDOW: Duration = 60.seconds
    }
}
