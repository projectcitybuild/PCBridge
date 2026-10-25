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
import kotlinx.coroutines.future.await
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
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
) : BrigadierCommand, Listener {
    private data class PendingFill(
        val worldId: UUID,
        val border: WorldBorder,
        val expiresAt: TimeMark,
    )

    private val pendingFills = mutableMapOf<UUID, PendingFill>()
    private val fillingWorlds = mutableSetOf<UUID>()

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        pendingFills.remove(event.player.uniqueId)
    }

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
        val geometry = border.geometry
        val chunkCount = geometry.chunkCoordinates()
            .take(MAX_CHUNKS_PER_FILL.toInt() + 1)
            .count()
            .toLong()
        if (chunkCount > MAX_CHUNKS_PER_FILL) {
            player.sendMessage(l10n.worlds.fillTooLarge(MAX_CHUNKS_PER_FILL))
            return
        }

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

        val geometry = pending.border.geometry
        var checked = 0L
        var generated = 0L
        try {
            player.sendMessage(l10n.worlds.fillStarted)

            for (chunk in geometry.chunkCoordinates()) {
                if (!player.isOnline) return
                if (worldBorderRepository.get(world) != pending.border) {
                    player.sendMessage(l10n.worlds.fillStoppedBorderChanged)
                    return
                }
                val existingChunk = world.getChunkAtAsync(chunk.x, chunk.z, false).await()
                if (existingChunk == null) {
                    world.getChunkAtAsync(chunk.x, chunk.z, true).await()
                    generated++
                }
                checked++

                if (checked % CHUNKS_PER_BATCH == 0L) {
                    player.sendMessage(l10n.worlds.fillProgress(checked, generated))
                    delay(BATCH_DELAY)
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

    companion object {
        private const val MAX_CHUNKS_PER_FILL = 50_000L
        private const val CHUNKS_PER_BATCH = 256L
        private val BATCH_DELAY: Duration = 1.milliseconds
        private val CONFIRMATION_WINDOW: Duration = 60.seconds
    }
}
