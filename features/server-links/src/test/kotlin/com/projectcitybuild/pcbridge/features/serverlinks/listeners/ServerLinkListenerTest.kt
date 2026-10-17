package com.projectcitybuild.pcbridge.features.serverlinks.listeners

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import net.kyori.adventure.text.Component
import org.bukkit.ServerLinks
import org.bukkit.event.player.PlayerLinksSendEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.net.URI

class ServerLinkListenerTest {
    private val remoteConfig = mock(RemoteConfig::class.java)
    private val listener = ServerLinkListener(remoteConfig)

    @Test
    fun `adds configured website and custom links`() {
        val links = mock(ServerLinks::class.java)
        val event = mock(PlayerLinksSendEvent::class.java)
        whenever(event.links).thenReturn(links)
        whenever(remoteConfig.latest).thenReturn(
            RemoteConfigVersion(
                version = 1,
                config =
                    RemoteConfigKeyValues(
                        serverLinks =
                            RemoteConfigKeyValues.ServerLinks(
                                website = "https://projectcitybuild.com",
                                custom = mapOf("Map" to "https://map.projectcitybuild.com"),
                            ),
                    ),
            ),
        )

        listener.oPlayerLinkSend(event)

        verify(links).setLink(
            ServerLinks.Type.WEBSITE,
            URI("https://projectcitybuild.com"),
        )
        verify(links).addLink(Component.text("Map"), URI("https://map.projectcitybuild.com"))
    }

    @Test
    fun `does not add links when none are configured`() {
        val links = mock(ServerLinks::class.java)
        val event = mock(PlayerLinksSendEvent::class.java)
        whenever(event.links).thenReturn(links)
        whenever(remoteConfig.latest).thenReturn(
            RemoteConfigVersion(version = 1, config = RemoteConfigKeyValues()),
        )

        listener.oPlayerLinkSend(event)

        verifyNoInteractions(links)
    }
}
