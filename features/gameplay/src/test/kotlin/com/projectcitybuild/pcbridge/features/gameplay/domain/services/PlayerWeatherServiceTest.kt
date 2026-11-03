package com.projectcitybuild.pcbridge.features.gameplay.domain.services

import org.bukkit.WeatherType
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PlayerWeatherServiceTest {
    private val service = PlayerWeatherService()

    @Test
    fun `returns the player's personal weather`() {
        val player = mock<Player>()
        whenever(player.playerWeather).thenReturn(WeatherType.DOWNFALL)

        assertEquals(WeatherType.DOWNFALL, service.current(player))
    }

    @Test
    fun `returns null when the player has no personal weather override`() {
        val player = mock<Player>()
        whenever(player.playerWeather).thenReturn(null)

        assertNull(service.current(player))
    }

    @Test
    fun `sets the player's personal weather`() {
        val player = mock<Player>()

        service.set(player, WeatherType.CLEAR)

        verify(player).setPlayerWeather(WeatherType.CLEAR)
    }

    @Test
    fun `resets the player's personal weather`() {
        val player = mock<Player>()

        service.reset(player)

        verify(player).resetPlayerWeather()
    }
}
