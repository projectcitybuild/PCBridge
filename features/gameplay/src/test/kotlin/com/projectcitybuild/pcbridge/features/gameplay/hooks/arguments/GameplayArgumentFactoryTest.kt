package com.projectcitybuild.pcbridge.features.gameplay.hooks.arguments

import com.mojang.brigadier.StringReader
import com.projectcitybuild.pcbridge.features.gameplay.domain.data.WeatherMode
import com.projectcitybuild.pcbridge.platform.paper.commands.arguments.Argument
import org.bukkit.GameMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GameplayArgumentFactoryTest {
    @Test
    fun `game mode argument accepts lowercase values`() {
        val argument = Argument.gameMode("mode")

        assertEquals(GameMode.SURVIVAL, argument.type.parse(StringReader("survival")))
    }

    @Test
    fun `weather argument accepts lowercase values`() {
        val argument = Argument.weather("weather")

        assertEquals(WeatherMode.THUNDER, argument.type.parse(StringReader("thunder")))
    }
}
