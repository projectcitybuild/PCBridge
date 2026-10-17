package com.projectcitybuild.pcbridge.features.stats.hooks.listeners

import com.projectcitybuild.pcbridge.features.stats.domain.StatsCollector
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class BlockChangeListenerTest {
    private val statsCollector = mock(StatsCollector::class.java)
    private val listener = BlockChangeListener(statsCollector)

    @Test
    fun `records placed blocks for the player`() {
        val playerUuid = UUID.randomUUID()
        val event = mock(BlockPlaceEvent::class.java)
        whenever(event.player).thenReturn(mock(Player::class.java))
        whenever(event.player.uniqueId).thenReturn(playerUuid)

        listener.onBlockPlace(event)

        verify(statsCollector).blockPlaced(playerUuid)
    }

    @Test
    fun `records destroyed blocks for the player`() {
        val playerUuid = UUID.randomUUID()
        val event = mock(BlockBreakEvent::class.java)
        whenever(event.player).thenReturn(mock(Player::class.java))
        whenever(event.player.uniqueId).thenReturn(playerUuid)

        listener.onBlockBreak(event)

        verify(statsCollector).blockDestroyed(playerUuid)
    }
}
