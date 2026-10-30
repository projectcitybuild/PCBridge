package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.gameplay.GameplayPermissions
import kotlinx.coroutines.test.runTest
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class WeatherCommandTest {
    @Test
    fun `sets storm weather and its duration in the sender's world`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val world = mock(World::class.java)
        whenever(world.name).thenReturn("world")
        whenever(harness.sender.world).thenReturn(world)
        val command = WeatherCommand(harness.plugin, harness.worldWeatherService, harness.executor)

        harness.execute(command, "weather storm 30")

        verify(world).setStorm(true)
        verify(world).weatherDuration = 600
        verify(harness.sender).sendMessage(l10n.gameplay.weatherSet("world", "storm", 30))
    }

    @Test
    fun `sets weather in a selected world`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val world = mock(World::class.java)
        whenever(world.name).thenReturn("world")
        whenever(harness.server.getWorld("world")).thenReturn(world)
        val command = WeatherCommand(harness.plugin, harness.worldWeatherService, harness.executor)

        harness.execute(command, "weather world sun")

        verify(world).setStorm(false)
        verify(harness.sender).sendMessage(l10n.gameplay.weatherSet("world", "sun", null))
    }

    @Test
    fun `requires an explicit world for console weather changes`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val console = mock(CommandSender::class.java)
        whenever(console.hasPermission(GameplayPermissions.WEATHER.node)).thenReturn(true)
        val command = WeatherCommand(harness.plugin, harness.worldWeatherService, harness.executor)
        harness.execute(command, "weather storm", console)
        verify(console).sendMessage(l10n.gameplay.errorWeatherWorldRequired)
    }
}
