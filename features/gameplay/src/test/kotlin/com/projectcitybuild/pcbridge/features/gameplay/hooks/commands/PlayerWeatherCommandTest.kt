package com.projectcitybuild.pcbridge.features.gameplay.hooks.commands

import com.projectcitybuild.pcbridge.core.l10n.l10n
import kotlinx.coroutines.test.runTest
import org.bukkit.WeatherType
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class PlayerWeatherCommandTest {
    @Test
    fun `shows a target's personal weather`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        whenever(harness.target.playerWeather).thenReturn(WeatherType.CLEAR)
        val command = PlayerWeatherCommand(harness.plugin, harness.server, harness.playerWeatherService, harness.executor)

        harness.execute(command, "pweather get Alex")

        verify(harness.sender).sendMessage(l10n.gameplay.playerWeatherCurrent("Alex", "clear"))
    }

    @Test
    fun `sets storm weather for a target player`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val command = PlayerWeatherCommand(harness.plugin, harness.server, harness.playerWeatherService, harness.executor)

        harness.execute(command, "pweather storm Alex")

        verify(harness.target).setPlayerWeather(WeatherType.DOWNFALL)
        verify(harness.sender).sendMessage(l10n.gameplay.playerWeatherSet("Alex", "storm"))
    }

    @Test
    fun `resets a target player's weather`() = runTest {
        val harness = GameplayCommandTestHarness(this)
        val command = PlayerWeatherCommand(harness.plugin, harness.server, harness.playerWeatherService, harness.executor)

        harness.execute(command, "pweather reset Alex")

        verify(harness.target).resetPlayerWeather()
        verify(harness.sender).sendMessage(l10n.gameplay.playerWeatherReset("Alex"))
    }
}
