package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.gameplay.GameplayPermissions
import kotlinx.coroutines.test.runTest
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class TimeCommandTest {
    @Test
    fun `requires the time permission before execution`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        whenever(harness.sender.hasPermission(GameplayPermissions.TIME.node)).thenReturn(false)
        val command = TimeCommand(harness.plugin, harness.worldTimeService, harness.executor)

        val failure = runCatching { harness.execute(command, "time set noon") }.exceptionOrNull()

        assertTrue(failure is CommandSyntaxException)
    }

    @Test
    fun `shows the current world time`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val world = mock(World::class.java)
        whenever(world.name).thenReturn("world")
        whenever(world.time).thenReturn(6_000L)
        whenever(harness.sender.world).thenReturn(world)
        val command = TimeCommand(harness.plugin, harness.worldTimeService, harness.executor)

        harness.execute(command, "time")

        verify(harness.sender).sendMessage(l10n.gameplay.worldTimeCurrent("world", "6000 ticks"))
    }

    @Test
    fun `sets world time to the next requested time`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val world = mock(World::class.java)
        whenever(world.name).thenReturn("world")
        whenever(world.fullTime).thenReturn(100L)
        whenever(harness.sender.world).thenReturn(world)
        val command = TimeCommand(harness.plugin, harness.worldTimeService, harness.executor)

        harness.execute(command, "time set noon")

        verify(world).fullTime = 30_000L
        verify(harness.sender).sendMessage(l10n.gameplay.worldTimeSet("world", "6000 ticks"))
    }

    @Test
    fun `sets world time from a colon-formatted value`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val world = mock(World::class.java)
        whenever(world.name).thenReturn("world")
        whenever(world.fullTime).thenReturn(100L)
        whenever(harness.sender.world).thenReturn(world)
        val command = TimeCommand(harness.plugin, harness.worldTimeService, harness.executor)

        harness.execute(command, "time set 06:30")

        verify(world).fullTime = 24_500L
        verify(harness.sender).sendMessage(l10n.gameplay.worldTimeSet("world", "500 ticks"))
    }

    @Test
    fun `adds ticks to the current world time`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val world = mock(World::class.java)
        whenever(world.name).thenReturn("world")
        whenever(world.fullTime).thenReturn(100L)
        whenever(harness.sender.world).thenReturn(world)
        val command = TimeCommand(harness.plugin, harness.worldTimeService, harness.executor)

        harness.execute(command, "time add 50")

        verify(world).fullTime = 150L
        verify(harness.sender).sendMessage(l10n.gameplay.worldTimeAdded("world", "50 ticks"))
    }

    @Test
    fun `adds a full day of numeric ticks without wrapping`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val world = mock(World::class.java)
        whenever(world.name).thenReturn("world")
        whenever(world.fullTime).thenReturn(100L)
        whenever(harness.sender.world).thenReturn(world)
        val command = TimeCommand(harness.plugin, harness.worldTimeService, harness.executor)

        harness.execute(command, "time add 24000")

        verify(world).fullTime = 24_100L
        verify(harness.sender).sendMessage(l10n.gameplay.worldTimeAdded("world", "24000 ticks"))
    }

    @Test
    fun `star selector sets time in all worlds`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val firstWorld = mock(World::class.java)
        val secondWorld = mock(World::class.java)
        whenever(firstWorld.name).thenReturn("world")
        whenever(secondWorld.name).thenReturn("nether")
        whenever(firstWorld.fullTime).thenReturn(100L)
        whenever(secondWorld.fullTime).thenReturn(200L)
        whenever(harness.server.worlds).thenReturn(listOf(firstWorld, secondWorld))
        val command = TimeCommand(harness.plugin, harness.worldTimeService, harness.executor)

        harness.execute(command, "time set noon *")

        verify(firstWorld).fullTime = 30_000L
        verify(secondWorld).fullTime = 30_000L
        verify(harness.sender).sendMessage(l10n.gameplay.worldTimeSet("world", "6000 ticks"))
        verify(harness.sender).sendMessage(l10n.gameplay.worldTimeSet("nether", "6000 ticks"))
    }

    @Test
    fun `console time query reports all worlds`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val firstWorld = mock(World::class.java)
        val secondWorld = mock(World::class.java)
        val console = mock(CommandSender::class.java)
        whenever(console.hasPermission(GameplayPermissions.TIME.node)).thenReturn(true)
        whenever(firstWorld.name).thenReturn("world")
        whenever(secondWorld.name).thenReturn("nether")
        whenever(firstWorld.time).thenReturn(100L)
        whenever(secondWorld.time).thenReturn(200L)
        whenever(harness.server.worlds).thenReturn(listOf(firstWorld, secondWorld))
        val command = TimeCommand(harness.plugin, harness.worldTimeService, harness.executor)

        harness.execute(command, "time", console)

        verify(console).sendMessage(l10n.gameplay.worldTimeCurrent("world", "100 ticks"))
        verify(console).sendMessage(l10n.gameplay.worldTimeCurrent("nether", "200 ticks"))
    }
}
