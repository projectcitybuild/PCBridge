package com.projectcitybuild.pcbridge.features.gameplay.domain.services

import org.bukkit.WeatherType
import org.bukkit.entity.Player

class PlayerWeatherService {
    fun current(player: Player): WeatherType? = player.playerWeather

    fun set(
        player: Player,
        weather: WeatherType,
    ) {
        player.setPlayerWeather(weather)
    }

    fun reset(player: Player) {
        player.resetPlayerWeather()
    }
}
