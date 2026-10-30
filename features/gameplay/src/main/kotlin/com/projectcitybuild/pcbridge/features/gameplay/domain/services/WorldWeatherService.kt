package com.projectcitybuild.pcbridge.features.gameplay.domain.services

import org.bukkit.Server
import org.bukkit.World

class WorldWeatherService(
    private val server: Server,
) {
    fun resolveWorld(
        selector: String?,
        defaultWorld: World?,
    ): WorldWeatherResolution {
        return when {
            selector != null -> server.getWorld(selector)?.let(WorldWeatherResolution::Found)
                ?: WorldWeatherResolution.Missing(selector)
            defaultWorld != null -> WorldWeatherResolution.Found(defaultWorld)
            else -> WorldWeatherResolution.RequiresWorld
        }
    }

    fun set(
        world: World,
        isStorm: Boolean,
        durationSeconds: Int?,
    ) {
        world.setStorm(isStorm)
        if (durationSeconds != null) {
            world.weatherDuration = durationSeconds * TICKS_PER_SECOND
        }
    }

    private companion object {
        const val TICKS_PER_SECOND = 20
    }
}

sealed interface WorldWeatherResolution {
    data class Found(
        val world: World,
    ) : WorldWeatherResolution

    data class Missing(
        val name: String,
    ) : WorldWeatherResolution

    data object RequiresWorld : WorldWeatherResolution
}
