package com.projectcitybuild.pcbridge.features.pim.hooks.listener

import com.projectcitybuild.pcbridge.features.pim.domain.services.OpElevationService
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class OpRestoreListenerTest {
    private val service = mock(OpElevationService::class.java)
    private val listener = OpRestoreListener(service)

    @Test
    fun `restores elevation on player join`() {
        val player = mock(Player::class.java)
        val event = mock(PlayerJoinEvent::class.java)
        whenever(event.player).thenReturn(player)

        listener.onPlayerJoin(event)

        verify(service).handleJoin(player)
    }

    @Test
    fun `cleans up elevation scheduling on player quit`() {
        val playerId = UUID.randomUUID()
        val player = mock(Player::class.java)
        val event = mock(PlayerQuitEvent::class.java)
        whenever(event.player).thenReturn(player)
        whenever(player.uniqueId).thenReturn(playerId)

        listener.onPlayerQuit(event)

        verify(service).handleLeave(playerId)
    }
}
