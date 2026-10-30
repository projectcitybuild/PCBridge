package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import kotlinx.coroutines.test.runTest
import org.bukkit.World
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class PlayerTimeCommandTest {
    @Test
    fun `shows normal time when the relative offset is zero`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        whenever(harness.target.playerTimeOffset).thenReturn(0L)
        whenever(harness.target.isPlayerTimeRelative).thenReturn(true)
        val command = PlayerTimeCommand(harness.plugin, harness.server, harness.playerTimeService, harness.executor)

        harness.execute(command, "ptime get Alex")

        verify(harness.sender).sendMessage(l10n.gameplay.playerTimeNormal("Alex"))
    }

    @Test
    fun `shows a fixed personal time even when its offset is zero`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        whenever(harness.target.playerTimeOffset).thenReturn(0L)
        whenever(harness.target.playerTime).thenReturn(1_000L)
        whenever(harness.target.isPlayerTimeRelative).thenReturn(false)
        val command = PlayerTimeCommand(harness.plugin, harness.server, harness.playerTimeService, harness.executor)

        harness.execute(command, "ptime get Alex")

        verify(harness.sender).sendMessage(
            l10n.gameplay.playerTimeCurrent("Alex", "1000 ticks", false),
        )
    }

    @Test
    fun `shows a target's personal time`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        whenever(harness.target.playerTimeOffset).thenReturn(50L)
        whenever(harness.target.playerTime).thenReturn(1_000L)
        whenever(harness.target.isPlayerTimeRelative).thenReturn(true)
        val command = PlayerTimeCommand(harness.plugin, harness.server, harness.playerTimeService, harness.executor)

        harness.execute(command, "ptime get Alex")

        verify(harness.sender).sendMessage(
            l10n.gameplay.playerTimeCurrent("Alex", "1000 ticks", true),
        )
    }

    @Test
    fun `sets a relative personal time`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val world = mock(World::class.java)
        whenever(world.time).thenReturn(100L)
        whenever(harness.target.world).thenReturn(world)
        val command = PlayerTimeCommand(harness.plugin, harness.server, harness.playerTimeService, harness.executor)

        harness.execute(command, "ptime noon Alex")

        verify(harness.target).setPlayerTime(29_900L, true)
        verify(harness.sender).sendMessage(l10n.gameplay.playerTimeSet("Alex", "6000 ticks", true))
    }

    @Test
    fun `sets a fixed personal time with the at prefix`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val command = PlayerTimeCommand(harness.plugin, harness.server, harness.playerTimeService, harness.executor)

        harness.execute(command, "ptime @noon Alex")

        verify(harness.target).setPlayerTime(30_000L, false)
        verify(harness.sender).sendMessage(l10n.gameplay.playerTimeSet("Alex", "6000 ticks", false))
    }

    @Test
    fun `sets a fixed personal time from a colon-formatted value`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val command = PlayerTimeCommand(harness.plugin, harness.server, harness.playerTimeService, harness.executor)

        harness.execute(command, "ptime @06:30 Alex")

        verify(harness.target).setPlayerTime(24_500L, false)
        verify(harness.sender).sendMessage(l10n.gameplay.playerTimeSet("Alex", "500 ticks", false))
    }

    @Test
    fun `reports a missing target player`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val command = PlayerTimeCommand(harness.plugin, harness.server, harness.playerTimeService, harness.executor)

        harness.execute(command, "ptime noon Unknown")

        verify(harness.sender).sendMessage(l10n.errors.errorPlayerNotFound("Unknown"))
    }

    @Test
    fun `resets a target's personal time`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val command = PlayerTimeCommand(harness.plugin, harness.server, harness.playerTimeService, harness.executor)

        harness.execute(command, "ptime reset Alex")

        verify(harness.target).resetPlayerTime()
        verify(harness.sender).sendMessage(l10n.gameplay.playerTimeReset("Alex"))
    }
}
