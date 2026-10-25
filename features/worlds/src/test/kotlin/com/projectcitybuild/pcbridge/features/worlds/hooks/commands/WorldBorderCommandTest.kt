package com.projectcitybuild.pcbridge.features.worlds.hooks.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.projectcitybuild.pcbridge.features.worlds.WorldsPermissions
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderUpdatedEvent
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.platform.paper.commands.BrigadierCommand
import com.projectcitybuild.pcbridge.platform.paper.commands.CoroutineScopeCommandTaskExecutor
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class WorldBorderCommandTest {
    private val player = mock(Player::class.java)
    private val worldBorderRepository = mock(WorldBorderRepository::class.java)
    private val eventBroadcaster = mock(SpigotEventBroadcaster::class.java)
    private val plugin = mock(Plugin::class.java)

    @Test
    fun `sets the border at the player's current location by default`() = runTest {
        val world = mock(World::class.java)
        whenever(player.world).thenReturn(world)
        whenever(player.location).thenReturn(Location(world, 12.5, 64.0, -8.0))
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        val command = command(this)

        execute(command, "border set square 96", source())
        advanceUntilIdle()

        val borderCaptor = argumentCaptor<WorldBorder>()
        verify(worldBorderRepository).set(eq(world), borderCaptor.capture())
        assertEquals(
            WorldBorder.Square(
                12.5,
                -8.0,
                radius = 96.0,
            ),
            borderCaptor.firstValue,
        )
        val eventCaptor = argumentCaptor<Event>()
        verify(eventBroadcaster).broadcast(eventCaptor.capture())
        val event = eventCaptor.firstValue as WorldBorderUpdatedEvent
        assertEquals(world, event.world)
        assertEquals(borderCaptor.firstValue, event.border)
    }

    @Test
    fun `sets a circular border with an explicit center`() = runTest {
        val world = mock(World::class.java)
        whenever(player.world).thenReturn(world)
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        val command = command(this)

        execute(command, "border set circle 48 -10 15", source())
        advanceUntilIdle()

        val borderCaptor = argumentCaptor<WorldBorder>()
        verify(worldBorderRepository).set(eq(world), borderCaptor.capture())
        assertEquals(
            WorldBorder.Circle(
                -10.0,
                15.0,
                radius = 48.0,
            ),
            borderCaptor.firstValue,
        )
    }

    @Test
    fun `sets a rectangular border with explicit width depth and center`() = runTest {
        val world = mock(World::class.java)
        whenever(player.world).thenReturn(world)
        whenever(player.hasPermission(WorldsPermissions.MANAGE.node)).thenReturn(true)
        val command = command(this)

        execute(command, "border set rectangle 80 40 -10 15", source())
        advanceUntilIdle()

        val borderCaptor = argumentCaptor<WorldBorder>()
        verify(worldBorderRepository).set(eq(world), borderCaptor.capture())
        assertEquals(
            WorldBorder.Rectangle(
                -10.0,
                15.0,
                width = 80.0,
                depth = 40.0,
            ),
            borderCaptor.firstValue,
        )
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
                BorderClearCommand(worldBorderRepository),
            )

        execute(command, "worlds clear", source())
        advanceUntilIdle()

        verify(worldBorderRepository).clear(world)
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
                worldBorderRepository,
                eventBroadcaster,
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
