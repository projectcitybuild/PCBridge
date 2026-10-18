package com.projectcitybuild.pcbridge.features.onboarding.hooks.listeners

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class AnnounceJoinListenerTest {
    private val remoteConfig = mock(RemoteConfig::class.java)
    private val listener = AnnounceJoinListener(remoteConfig)

    @Test
    fun `formats configured join announcement with player name`() {
        val player = mock(Player::class.java)
        val event = mock(PlayerJoinEvent::class.java)
        whenever(player.name).thenReturn("Alex")
        whenever(event.player).thenReturn(player)
        whenever(remoteConfig.latest).thenReturn(
            RemoteConfigVersion(
                1,
                RemoteConfigKeyValues(messages = RemoteConfigKeyValues.Messages(join = "<green><name> joined</green>")),
            ),
        )

        listener.onPlayerJoin(event)

        verify(event).joinMessage(MiniMessage.miniMessage().deserialize("<green>Alex joined</green>"))
    }
}
