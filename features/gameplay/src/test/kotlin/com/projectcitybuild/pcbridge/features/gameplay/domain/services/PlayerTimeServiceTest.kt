package com.projectcitybuild.pcbridge.features.gameplay.domain.services

import org.bukkit.World
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PlayerTimeServiceTest {
    private val service = PlayerTimeService()

    @Test
    fun `sets time relative to the world by default`() {
        val player = mock<Player>()
        val world = mock<World>()
        whenever(player.playerTime).thenReturn(100L)
        whenever(player.world).thenReturn(world)
        whenever(world.time).thenReturn(1_000L)

        val result = service.set(player, "noon")

        assertEquals(PlayerTimeService.SetResult.Updated(6_000L, true), result)
        verify(player).setPlayerTime(29_000L, true)
    }

    @Test
    fun `sets time absolutely when prefixed with at sign`() {
        val player = mock<Player>()
        whenever(player.playerTime).thenReturn(100L)

        val result = service.set(player, "@noon")

        assertEquals(PlayerTimeService.SetResult.Updated(6_000L, false), result)
        verify(player).setPlayerTime(30_000L, false)
    }

    @Test
    fun `reports invalid time without changing player time`() {
        val player = mock<Player>()

        val result = service.set(player, "not-a-time")

        assertEquals(PlayerTimeService.SetResult.InvalidTime("not-a-time"), result)
        verify(player, never()).setPlayerTime(org.mockito.kotlin.any(), org.mockito.kotlin.any())
    }

    @Test
    fun `validates absolute and relative input`() {
        assertTrue(service.isValid("noon"))
        assertTrue(service.isValid("@noon"))
        assertTrue(service.isValid("+50"))
        assertFalse(service.isValid("not-a-time"))
    }

    @Test
    fun `reports overflow without changing player time`() {
        val player = mock<Player>()
        whenever(player.playerTime).thenReturn(Long.MAX_VALUE)

        val result = service.set(player, "@noon")

        assertEquals(PlayerTimeService.SetResult.InvalidTime("@noon"), result)
        verify(player, never()).setPlayerTime(org.mockito.kotlin.any(), org.mockito.kotlin.any())
    }
}
