package com.projectcitybuild.pcbridge.features.onboarding.hooks.listeners

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class ServerOverviewJoinListenerTest {
    private val remoteConfig = mock(RemoteConfig::class.java)
    private val listener = ServerOverviewJoinListener(remoteConfig)

    @Test
    fun `sends configured welcome message to joining player`() {
        val player = mock(Player::class.java)
        val event = mock(PlayerJoinEvent::class.java)
        whenever(event.player).thenReturn(player)
        whenever(remoteConfig.latest).thenReturn(
            RemoteConfigVersion(1, RemoteConfigKeyValues(messages = RemoteConfigKeyValues.Messages(welcome = "<green>Welcome!"))),
        )

        listener.onPlayerJoin(event)

        verify(player).sendRichMessage("<green>Welcome!")
    }
}
