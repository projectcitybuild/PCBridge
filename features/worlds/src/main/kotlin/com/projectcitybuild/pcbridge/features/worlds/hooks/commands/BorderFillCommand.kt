package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.features.worlds.WorldsPermissions
import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.PaperCommandContext
import com.projectcitybuild.pcbridge.platform.paper.commands.PluginCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.command
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.executesSuspending
import com.projectcitybuild.pcbridge.platform.paper.commands.extensions.requirePlayer
import com.projectcitybuild.pcbridge.platform.paper.commands.requiresPermission
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.World
import org.bukkit.plugin.Plugin

class BorderFillCommand(
    private val plugin: Plugin,
    private val worldBorderRepository: WorldBorderRepository,
    private val taskExecutor: CommandTaskExecutor = PluginCommandTaskExecutor(plugin),
) : BrigadierCommand {
    private data class PendingFill(
        val worldId: UUID,
        val border: SerializableWorldBorder,
        val expiresAtMillis: Long,
    )

    private val pendingFills = mutableMapOf<UUID, PendingFill>()
    private val fillingWorlds = mutableSetOf<UUID>()

    override val builder =
        command("fill") {
            description = "Generates missing chunks inside the world border"
            requiresPermission(WorldsPermissions.MANAGE)
            executesSuspending(taskExecutor, ::preview)
            literal("confirm") {
                executesSuspending(taskExecutor, ::confirm)
            }
        }

    private fun preview(context: PaperCommandContext) {
        val player = context.source.requirePlayer()
        pendingFills.entries.removeIf { it.value.expiresAtMillis < System.currentTimeMillis() }
        val border =
            worldBorderRepository.get(player.world)
                ?: run {
                    player.sendMessage("Set a world border before filling chunks.")
                    return
                }
        val bounds = chunkBounds(border)
        val chunkCount =
            (bounds.maxX.toLong() - bounds.minX + 1) *
                (bounds.maxZ.toLong() - bounds.minZ + 1)
        pendingFills[player.uniqueId] =
            PendingFill(
                worldId = player.world.uid,
                border = border,
                expiresAtMillis = System.currentTimeMillis() + CONFIRMATION_WINDOW_MILLIS,
            )
        player.sendMessage(
            "Fill preview: up to $chunkCount chunks may be checked in ${player.world.name}. " +
                "Only missing chunks will be generated. Run /worlds border fill confirm within 60 seconds to continue.",
        )
    }

    private suspend fun confirm(context: PaperCommandContext) {
        val player = context.source.requirePlayer()
        val pending = pendingFills.remove(player.uniqueId)
        if (pending == null || pending.expiresAtMillis < System.currentTimeMillis()) {
            player.sendMessage("There is no active fill preview. Run /worlds border fill first.")
            return
        }
        if (pending.worldId != player.world.uid) {
            player.sendMessage("You must confirm the fill in the world where it was previewed.")
            return
        }
        if (worldBorderRepository.get(player.world) != pending.border) {
            player.sendMessage("The world border changed. Run /worlds border fill again to preview the updated area.")
            return
        }

        val world = player.world
        if (!fillingWorlds.add(world.uid)) {
            player.sendMessage("A world border fill is already running for this world.")
            return
        }

        val bounds = chunkBounds(pending.border)
        var checked = 0L
        var generated = 0L
        try {
            player.sendMessage("Starting world border fill...")

            for (chunkX in bounds.minX..bounds.maxX) {
                val chunkZRange = pending.border.chunkZRange(chunkX) ?: continue
                for (chunkZ in chunkZRange) {
                    if (!pending.border.chunkIntersects(chunkX, chunkZ)) continue
                    if (worldBorderRepository.get(world) != pending.border) {
                        player.sendMessage("World border changed during fill; the operation was stopped.")
                        return
                    }
                    if (!world.isChunkGenerated(chunkX, chunkZ)) {
                        loadOrGenerateChunk(world, chunkX, chunkZ)
                        generated++
                    }
                    checked++

                    if (checked % CHUNKS_PER_BATCH == 0L) {
                        player.sendMessage("World border fill progress: checked $checked chunks; generated $generated.")
                        delay(BATCH_DELAY_MILLIS)
                    }
                }
            }
            player.sendMessage("World border fill complete: checked $checked chunks; generated $generated.")
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            log.error(failure) { "World border fill failed for ${world.name}" }
            player.sendMessage("World border fill failed. See the server log for details.")
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

    private fun chunkBounds(border: SerializableWorldBorder): ChunkBounds =
        ChunkBounds(
            minX = kotlin.math.floor((border.centerX - border.radius) / CHUNK_SIZE).toInt(),
            maxX = kotlin.math.floor((border.centerX + border.radius) / CHUNK_SIZE).toInt(),
            minZ = kotlin.math.floor((border.centerZ - border.radius) / CHUNK_SIZE).toInt(),
            maxZ = kotlin.math.floor((border.centerZ + border.radius) / CHUNK_SIZE).toInt(),
        )

    companion object {
        private const val CHUNK_SIZE = 16.0
        private const val CHUNKS_PER_BATCH = 256L
        private const val BATCH_DELAY_MILLIS = 1L
        private const val CONFIRMATION_WINDOW_MILLIS = 60_000L
    }
}
