package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.worlds.WorldsPermissions
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.features.worlds.domain.services.WorldBorderService
import com.projectcitybuild.pcbridge.features.worlds.hooks.commands.BorderFillCommand.Companion.DEFAULT_CHUNK_LIMIT
import com.projectcitybuild.pcbridge.features.worlds.hooks.commands.BorderFillCommand.Companion.HARD_MAX_CHUNKS_PER_FILL
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import java.util.UUID
import java.util.concurrent.CompletableFuture

@OptIn(ExperimentalCoroutinesApi::class)
class WorldBorderCommandTest {
    private val player = mock(Player::class.java)
    private val worldBorderRepository = mock(WorldBorderRepository::class.java)
    private val worldBorderService = mock(WorldBorderService::class.java)
    private val plugin = mock(Plugin::class.java)

    @Test
    fun `sets the border at the player's current location by default`() = runTest {
        val world = mock(World::class.java)
        whenever(player.world).thenReturn(world)
        whenever(player.location).thenReturn(Location(world, 12.5, 64.0, -8.0))
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        whenever(worldBorderService.set(eq(world), any())).thenReturn(emptyList())
        val command = command(this)

        execute(command, "border set square 96", source())
        advanceUntilIdle()

        verify(worldBorderService).set(
            eq(world),
            eq(WorldBorder.Square(12.5, -8.0, radius = 96.0)),
        )
    }

    @Test
    fun `sets a circular border with an explicit center`() = runTest {
        val world = mock(World::class.java)
        whenever(player.world).thenReturn(world)
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        whenever(worldBorderService.set(eq(world), any())).thenReturn(emptyList())
        val command = command(this)

        execute(command, "border set circle 48 -10 15", source())
        advanceUntilIdle()

        verify(worldBorderService).set(
            eq(world),
            eq(WorldBorder.Circle(
                -10.0,
                15.0,
                radius = 48.0,
            )),
        )
    }

    @Test
    fun `sets a rectangular border with explicit width depth and center`() = runTest {
        val world = mock(World::class.java)
        whenever(player.world).thenReturn(world)
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        whenever(worldBorderService.set(eq(world), any())).thenReturn(listOf(UUID.randomUUID()))
        val command = command(this)

        execute(command, "border set rectangle 80 40 -10 15", source())
        advanceUntilIdle()

        verify(worldBorderService).set(
            eq(world),
            eq(WorldBorder.Rectangle(
                -10.0,
                15.0,
                width = 80.0,
                depth = 40.0,
            )),
        )
        verify(player).sendMessage(l10n.worlds.borderRelocationFailures(1))
    }

    @Test
    fun `requires a shape when setting a border`() = runTest {
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        val command = command(this)

        assertThrows<CommandSyntaxException> {
            execute(command, "border set 96", source())
        }
    }

    @Test
    fun `clears the border in the current world`() = runTest {
        val world = mock(World::class.java)
        whenever(player.world).thenReturn(world)
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        val command =
            WorldsCommand(
                command(this),
                BorderClearCommand(plugin, worldBorderService, CoroutineScopeCommandTaskExecutor(this)),
            )

        execute(command, "worlds clear", source())
        advanceUntilIdle()

        verify(worldBorderService).clear(world)
    }

    @Test
    fun `rejects a fill preview above the chunk limit`() = runTest {
        val world = mock(World::class.java)
        whenever(world.uid).thenReturn(java.util.UUID.randomUUID())
        whenever(world.name).thenReturn("world")
        whenever(player.world).thenReturn(world)
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        whenever(worldBorderRepository.get(world)).thenReturn(WorldBorder.Circle(0.0, 0.0, 2_200.0))
        val fill = BorderFillCommand(plugin, worldBorderRepository, CoroutineScopeCommandTaskExecutor(this))

        execute(fill, "fill", source())
        advanceUntilIdle()

        verify(player).sendMessage(l10n.worlds.fillTooLarge(DEFAULT_CHUNK_LIMIT.toLong()))
    }

    @Test
    fun `configured fill limit cannot exceed the hard maximum`() = runTest {
        val world = mock(World::class.java)
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(world.name).thenReturn("world")
        whenever(player.world).thenReturn(world)
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        whenever(worldBorderRepository.get(world)).thenReturn(
            WorldBorder.Rectangle(0.0, 0.0, width = 4_000.0, depth = 4_000.0),
        )
        val fill = BorderFillCommand(
            plugin,
            worldBorderRepository,
            CoroutineScopeCommandTaskExecutor(this),
            configuredChunkLimit = 60_000,
        )

        execute(fill, "fill", source())
        advanceUntilIdle()

        verify(player).sendMessage(l10n.worlds.fillTooLarge(HARD_MAX_CHUNKS_PER_FILL))
    }

    @Test
    fun `uses the configured fill limit below the hard maximum`() = runTest {
        val world = mock(World::class.java)
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(world.name).thenReturn("world")
        whenever(player.world).thenReturn(world)
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        whenever(worldBorderRepository.get(world)).thenReturn(
            WorldBorder.Rectangle(0.0, 0.0, width = 512.0, depth = 512.0),
        )
        val fill = BorderFillCommand(
            plugin,
            worldBorderRepository,
            CoroutineScopeCommandTaskExecutor(this),
            configuredChunkLimit = 100,
        )

        execute(fill, "fill", source())
        advanceUntilIdle()

        verify(player).sendMessage(l10n.worlds.fillTooLarge(100))
    }

    @Test
    fun `completes a confirmed fill and reports its progress`() = runTest {
        val world = mock(World::class.java)
        val worldId = UUID.randomUUID()
        val border = WorldBorder.Square(0.0, 0.0, radius = 8.0)
        whenever(world.uid).thenReturn(worldId)
        whenever(world.name).thenReturn("world")
        whenever(player.world).thenReturn(world)
        whenever(player.uniqueId).thenReturn(UUID.randomUUID())
        whenever(player.isOnline).thenReturn(true)
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        whenever(worldBorderRepository.get(world)).thenReturn(border)
        whenever(world.getChunkAtAsync(any<Int>(), any<Int>(), eq(false)))
            .thenReturn(CompletableFuture.completedFuture(mock(Chunk::class.java)))
        val fill = BorderFillCommand(
            plugin,
            worldBorderRepository,
            CoroutineScopeCommandTaskExecutor(this),
        )

        execute(fill, "fill", source())
        advanceUntilIdle()
        execute(fill, "fill confirm", source())
        advanceUntilIdle()

        verify(world, times(4)).getChunkAtAsync(any<Int>(), any<Int>(), eq(false))
        verify(world, never()).getChunkAtAsync(any<Int>(), any<Int>(), eq(true))
        verify(player).sendMessage(l10n.worlds.fillPreview(4, "world"))
        verify(player).sendMessage(l10n.worlds.fillComplete(4, 4, 0))
        execute(fill, "fill status", source())
        advanceUntilIdle()
        verify(player).sendMessage(l10n.worlds.fillNoActiveJob)
    }

    @Test
    fun `reports status and cancels an active fill`() = runTest {
        val world = mock(World::class.java)
        val border = WorldBorder.Square(0.0, 0.0, radius = 8.0)
        val pendingChunk = CompletableFuture<Chunk>()
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(world.name).thenReturn("world")
        whenever(player.world).thenReturn(world)
        whenever(player.uniqueId).thenReturn(UUID.randomUUID())
        whenever(player.isOnline).thenReturn(true)
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        whenever(worldBorderRepository.get(world)).thenReturn(border)
        whenever(world.getChunkAtAsync(any<Int>(), any<Int>(), eq(false))).thenReturn(pendingChunk)
        val fill = BorderFillCommand(
            plugin,
            worldBorderRepository,
            CoroutineScopeCommandTaskExecutor(this),
        )

        execute(fill, "fill", source())
        advanceUntilIdle()
        execute(fill, "fill confirm", source())
        runCurrent()
        execute(fill, "fill status", source())
        advanceUntilIdle()
        execute(fill, "fill cancel", source())
        advanceUntilIdle()

        verify(player).sendMessage(l10n.worlds.fillStatus(0, 4, 0, "running"))
        verify(player).sendMessage(l10n.worlds.fillCancelRequested)
        verify(player).sendMessage(l10n.worlds.fillCancelled(0, 4, 0))
        execute(fill, "fill status", source())
        advanceUntilIdle()
        verify(player).sendMessage(l10n.worlds.fillNoActiveJob)
    }

    @Test
    fun `stops before generating a chunk when the border changes during fill`() = runTest {
        val world = mock(World::class.java)
        val border = WorldBorder.Square(0.0, 0.0, radius = 8.0)
        val changedBorder = WorldBorder.Square(1.0, 0.0, radius = 8.0)
        val pendingChunk = CompletableFuture<Chunk>()
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(world.name).thenReturn("world")
        whenever(player.world).thenReturn(world)
        whenever(player.uniqueId).thenReturn(UUID.randomUUID())
        whenever(player.isOnline).thenReturn(true)
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        whenever(worldBorderRepository.get(world)).thenReturn(border)
        whenever(world.getChunkAtAsync(any<Int>(), any<Int>(), eq(false))).thenReturn(pendingChunk)
        val fill = BorderFillCommand(
            plugin,
            worldBorderRepository,
            CoroutineScopeCommandTaskExecutor(this),
        )

        execute(fill, "fill", source())
        advanceUntilIdle()
        execute(fill, "fill confirm", source())
        runCurrent()
        pendingChunk.complete(mock(Chunk::class.java))
        whenever(worldBorderRepository.get(world)).thenReturn(changedBorder)
        advanceUntilIdle()

        verify(world, never()).getChunkAtAsync(any<Int>(), any<Int>(), eq(true))
        verify(player).sendMessage(l10n.worlds.fillStoppedBorderChanged)
        execute(fill, "fill status", source())
        advanceUntilIdle()
        verify(player).sendMessage(l10n.worlds.fillNoActiveJob)
    }

    @Test
    fun `disconnect cancels the fill and releases its per-world guard`() = runTest {
        val world = mock(World::class.java)
        val border = WorldBorder.Square(0.0, 0.0, radius = 8.0)
        val pendingChunk = CompletableFuture<Chunk>()
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(world.name).thenReturn("world")
        whenever(player.world).thenReturn(world)
        whenever(player.uniqueId).thenReturn(UUID.randomUUID())
        whenever(player.isOnline).thenReturn(true)
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        whenever(worldBorderRepository.get(world)).thenReturn(border)
        whenever(world.getChunkAtAsync(any<Int>(), any<Int>(), eq(false))).thenReturn(pendingChunk)
        val fill = BorderFillCommand(
            plugin,
            worldBorderRepository,
            CoroutineScopeCommandTaskExecutor(this),
        )

        execute(fill, "fill", source())
        advanceUntilIdle()
        execute(fill, "fill confirm", source())
        runCurrent()
        whenever(player.isOnline).thenReturn(false)
        val quitEvent = mock(PlayerQuitEvent::class.java)
        whenever(quitEvent.player).thenReturn(player)
        fill.onPlayerQuit(quitEvent)
        advanceUntilIdle()

        whenever(player.isOnline).thenReturn(true)
        execute(fill, "fill status", source())
        advanceUntilIdle()
        verify(player).sendMessage(l10n.worlds.fillNoActiveJob)
    }

    @Test
    fun `world unload cancels the fill and releases its per-world guard`() = runTest {
        val world = mock(World::class.java)
        val worldId = UUID.randomUUID()
        val border = WorldBorder.Square(0.0, 0.0, radius = 8.0)
        val pendingChunk = CompletableFuture<Chunk>()
        whenever(world.uid).thenReturn(worldId)
        whenever(world.name).thenReturn("world")
        whenever(player.world).thenReturn(world)
        whenever(player.uniqueId).thenReturn(UUID.randomUUID())
        whenever(player.isOnline).thenReturn(true)
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        whenever(worldBorderRepository.get(world)).thenReturn(border)
        whenever(world.getChunkAtAsync(any<Int>(), any<Int>(), eq(false))).thenReturn(pendingChunk)
        val fill = BorderFillCommand(
            plugin,
            worldBorderRepository,
            CoroutineScopeCommandTaskExecutor(this),
        )

        execute(fill, "fill", source())
        advanceUntilIdle()
        execute(fill, "fill confirm", source())
        runCurrent()
        val unloadEvent = mock(WorldUnloadEvent::class.java)
        whenever(unloadEvent.world).thenReturn(world)
        fill.onWorldUnload(unloadEvent)
        advanceUntilIdle()

        execute(fill, "fill status", source())
        advanceUntilIdle()
        verify(player).sendMessage(l10n.worlds.fillNoActiveJob)
    }

    @Test
    fun `generation failure reports an error and releases its per-world guard`() = runTest {
        val world = mock(World::class.java)
        val border = WorldBorder.Square(0.0, 0.0, radius = 8.0)
        val failure = IllegalStateException("chunk generation failed")
        val failures = mutableListOf<Throwable>()
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(world.name).thenReturn("world")
        whenever(player.world).thenReturn(world)
        whenever(player.uniqueId).thenReturn(UUID.randomUUID())
        whenever(player.isOnline).thenReturn(true)
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        whenever(worldBorderRepository.get(world)).thenReturn(border)
        whenever(world.getChunkAtAsync(any<Int>(), any<Int>(), eq(false)))
            .thenReturn(CompletableFuture.completedFuture(null))
        whenever(world.getChunkAtAsync(any<Int>(), any<Int>(), eq(true)))
            .thenReturn(CompletableFuture<Chunk>().also { it.completeExceptionally(failure) })
        val taskExecutor = CommandTaskExecutor { task ->
            launch {
                try {
                    task()
                } catch (caught: Exception) {
                    failures += caught
                }
            }
        }
        val fill = BorderFillCommand(plugin, worldBorderRepository, taskExecutor)

        execute(fill, "fill", source())
        advanceUntilIdle()
        execute(fill, "fill confirm", source())
        advanceUntilIdle()

        assertEquals(listOf(failure), failures)
        verify(player).sendMessage(l10n.worlds.fillFailed)
        execute(fill, "fill status", source())
        advanceUntilIdle()
        verify(player).sendMessage(l10n.worlds.fillNoActiveJob)
    }

    private fun command(scope: CoroutineScope): WorldBorderCommand {
        val fillCommand =
            BorderFillCommand(
                plugin,
                worldBorderRepository,
                CoroutineScopeCommandTaskExecutor(scope),
            )
        val borderSetCommand =
            BorderSetCommand(
                plugin,
                worldBorderService,
                CoroutineScopeCommandTaskExecutor(scope),
            )
        return WorldBorderCommand(
            plugin,
            worldBorderRepository,
            borderSetCommand,
            fillCommand,
            CoroutineScopeCommandTaskExecutor(scope),
        )
    }

    private fun source(): CommandSourceStack = mock(CommandSourceStack::class.java).also {
        whenever(it.sender).thenReturn(player)
    }

    private fun execute(
        command: BrigadierCommand,
        input: String,
        source: CommandSourceStack,
    ) {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        dispatcher.root.addChild(command.literal())
        dispatcher.execute(input, source)
    }
}
