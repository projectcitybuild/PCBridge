package com.projectcitybuild.pcbridge.features.gameplay.domain.services

import org.bukkit.Server
import org.bukkit.World
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class WorldWeatherServiceTest {
    private val server = mock<Server>()
    private val service = WorldWeatherService(server)

    @Test
    fun `resolves a named world`() {
        val world = mock<World>()
        whenever(server.getWorld("nether")).thenReturn(world)

        assertEquals(WorldWeatherResolution.Found(world), service.resolveWorld("nether", null))
    }

    @Test
    fun `reports a missing named world`() {
        whenever(server.getWorld("missing")).thenReturn(null)

        assertEquals(WorldWeatherResolution.Missing("missing"), service.resolveWorld("missing", null))
    }

    @Test
    fun `uses the default world when no selector is provided`() {
        val world = mock<World>()

        assertEquals(WorldWeatherResolution.Found(world), service.resolveWorld(null, world))
    }

    @Test
    fun `requires a world when neither selector nor default is available`() {
        assertEquals(WorldWeatherResolution.RequiresWorld, service.resolveWorld(null, null))
    }

    @Test
    fun `sets storm and converts duration seconds to ticks`() {
        val world = mock<World>()

        service.set(world, isStorm = true, durationSeconds = 30)

        verify(world).setStorm(true)
        verify(world).weatherDuration = 600
    }
}
