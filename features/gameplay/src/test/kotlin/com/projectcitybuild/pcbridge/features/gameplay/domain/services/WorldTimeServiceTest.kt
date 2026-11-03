package com.projectcitybuild.pcbridge.features.gameplay.domain.services

import com.projectcitybuild.pcbridge.features.gameplay.domain.data.WorldSelector
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldTimeService.SetResult
import com.projectcitybuild.pcbridge.features.gameplay.domain.services.WorldTimeService.WorldTimeChange
import org.bukkit.Server
import org.bukkit.World
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class WorldTimeServiceTest {
    private val server = mock<Server>()
    private val service = WorldTimeService(server)

    @Test
    fun `sets absolute time in the default world at its next occurrence`() {
        val world = mock<World>()
        whenever(world.fullTime).thenReturn(100L)

        val result = service.set("noon", WorldSelector.Current, world)

        assertEquals(SetResult.Updated(listOf(WorldTimeChange(world, 6_000L, null))), result)
        verify(world).fullTime = 30_000L
    }

    @Test
    fun `applies relative time to every world`() {
        val firstWorld = mock<World>()
        val secondWorld = mock<World>()
        whenever(firstWorld.fullTime).thenReturn(100L)
        whenever(secondWorld.fullTime).thenReturn(200L)
        whenever(server.worlds).thenReturn(listOf(firstWorld, secondWorld))

        val result = service.set("+50", WorldSelector.All, null)

        assertEquals(
            SetResult.Updated(
                listOf(
                    WorldTimeChange(firstWorld, 150L, 50L),
                    WorldTimeChange(secondWorld, 250L, 50L),
                ),
            ),
            result,
        )
        verify(firstWorld).fullTime = 150L
        verify(secondWorld).fullTime = 250L
    }

    @Test
    fun `sets time in a named world`() {
        val world = mock<World>()
        whenever(world.fullTime).thenReturn(100L)
        whenever(server.getWorld("nether")).thenReturn(world)

        val result = service.set("morning", WorldSelector.Named("nether"), null)

        assertEquals(SetResult.Updated(listOf(WorldTimeChange(world, 1_000L, null))), result)
        verify(world).fullTime = 25_000L
    }

    @Test
    fun `reports invalid input without changing worlds`() {
        val result = service.set("not-a-time", WorldSelector.All, null)

        assertEquals(SetResult.InvalidTime("not-a-time"), result)
    }

    @Test
    fun `reports a missing named world`() {
        whenever(server.getWorld("missing")).thenReturn(null)

        val result = service.set("noon", WorldSelector.Named("missing"), null)

        assertEquals(SetResult.WorldNotFound("missing"), result)
    }

    @Test
    fun `requires a world when the current selector has no default`() {
        val result = service.set("noon", WorldSelector.Current, null)

        assertEquals(SetResult.WorldRequired, result)
    }

    @Test
    fun `does not partially update worlds when relative time overflows`() {
        val firstWorld = mock<World>()
        val overflowingWorld = mock<World>()
        whenever(firstWorld.fullTime).thenReturn(100L)
        whenever(overflowingWorld.fullTime).thenReturn(Long.MAX_VALUE)
        whenever(server.worlds).thenReturn(listOf(firstWorld, overflowingWorld))

        val result = service.set("+1", WorldSelector.All, null)

        assertEquals(SetResult.InvalidTime("+1"), result)
        verify(firstWorld, never()).setFullTime(any())
        verify(overflowingWorld, never()).setFullTime(any())
    }
}
