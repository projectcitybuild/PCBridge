package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.features.worlds.WorldsPermissions
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.geometry.WorldBorderGeometry
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.World
import org.bukkit.plugin.Plugin
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class BorderFillCommand(
    private val plugin: Plugin,
    private val worldBorderRepository: WorldBorderRepository,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    private data class PendingFill(
        val worldId: UUID,
        val border: WorldBorder,
        val expiresAt: TimeMark,
    )

    private val pendingFills = mutableMapOf<UUID, PendingFill>()
    private val fillingWorlds = mutableSetOf<UUID>()

    override val builder = command("fill") {
        description = l10n.worlds.borderFillCommandDescription
        requiresPermission(WorldsPermissions.MANAGE)
        executesSuspending(taskExecutor, ::preview)
        literal("confirm") {
            executesSuspending(taskExecutor, ::confirm)
        }
    }

    private fun preview(context: PaperCommandContext) {
        val player = context.source.requirePlayer()
        pendingFills.entries.removeIf {
            it.value.expiresAt.hasPassedNow()
        }
        val border = worldBorderRepository.get(player.world)
            ?: run {
                player.sendMessage(l10n.worlds.fillRequiresBorder)
                return
            }
        val geometry = WorldBorderGeometry(border)
        val bounds = chunkBounds(geometry)
        val chunkCount = (bounds.maxX.toLong() - bounds.minX + 1) *
            (bounds.maxZ.toLong() - bounds.minZ + 1)

        pendingFills[player.uniqueId] = PendingFill(
            worldId = player.world.uid,
            border = border,
            expiresAt = TimeSource.Monotonic.markNow() + CONFIRMATION_WINDOW,
        )
        player.sendMessage(l10n.worlds.fillPreview(chunkCount, player.world.name))
    }

    private suspend fun confirm(context: PaperCommandContext) {
        val player = context.source.requirePlayer()
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
        if (!fillingWorlds.add(world.uid)) {
            player.sendMessage(l10n.worlds.fillAlreadyRunning)
            return
        }

        val geometry = WorldBorderGeometry(pending.border)
        val bounds = chunkBounds(geometry)
        var checked = 0L
        var generated = 0L
        try {
            player.sendMessage(l10n.worlds.fillStarted)

            for (chunkX in bounds.minX..bounds.maxX) {
                val chunkZRange = geometry.chunkZRange(chunkX) ?: continue
                for (chunkZ in chunkZRange) {
                    if (!geometry.chunkIntersects(chunkX, chunkZ)) continue
                    if (worldBorderRepository.get(world) != pending.border) {
                        player.sendMessage(l10n.worlds.fillStoppedBorderChanged)
                        return
                    }
                    if (!world.isChunkGenerated(chunkX, chunkZ)) {
                        loadOrGenerateChunk(world, chunkX, chunkZ)
                        generated++
                    }
                    checked++

                    if (checked % CHUNKS_PER_BATCH == 0L) {
                        player.sendMessage(l10n.worlds.fillProgress(checked, generated))
                        delay(BATCH_DELAY)
                    }
                }
            }
            player.sendMessage(l10n.worlds.fillComplete(checked, generated))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            log.error(failure) { "World border fill failed for ${world.name}" }
            player.sendMessage(l10n.worlds.fillFailed)
            throw failure
        } finally {
            fillingWorlds.remove(world.uid)
        }
    }

    private suspend fun loadOrGenerateChunk(
        world: World,
        x: Int,
        z: Int,
    ) = suspendCancellableCoroutine { continuation ->
        world.getChunkAtAsync(x, z, true).whenComplete { _, failure ->
            if (failure == null) {
                continuation.resumeWith(Result.success(Unit))
            } else {
                continuation.resumeWith(Result.failure(failure))
            }
        }
    }

    private data class ChunkBounds(
        val minX: Int,
        val maxX: Int,
        val minZ: Int,
        val maxZ: Int,
    )

    private fun chunkBounds(geometry: WorldBorderGeometry): ChunkBounds = ChunkBounds(
        minX = kotlin.math.floor(geometry.minX / CHUNK_SIZE).toInt(),
        maxX = kotlin.math.floor(geometry.maxX / CHUNK_SIZE).toInt(),
        minZ = kotlin.math.floor(geometry.minZ / CHUNK_SIZE).toInt(),
        maxZ = kotlin.math.floor(geometry.maxZ / CHUNK_SIZE).toInt(),
    )

    companion object {
        private const val CHUNK_SIZE = 16.0
        private const val CHUNKS_PER_BATCH = 256L
        private val BATCH_DELAY: Duration = 1.milliseconds
        private val CONFIRMATION_WINDOW: Duration = 60.seconds
    }
}
