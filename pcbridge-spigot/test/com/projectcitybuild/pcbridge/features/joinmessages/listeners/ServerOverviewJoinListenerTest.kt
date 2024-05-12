package com.projectcitybuild.pcbridge.features.joinmessages.listeners

import com.projectcitybuild.pcbridge.data.PluginConfig
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.pcbridge.features.joinmessages.listeners.ServerOverviewJoinListener
import kotlinx.coroutines.test.runTest
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class ServerOverviewJoinListenerTest {
    private lateinit var player: Player
    private lateinit var spigotPlayer: Player.Spigot
    private lateinit var config: Config<PluginConfig>

    @BeforeEach
    fun setUp() {
        player = mock(Player::class.java)
        spigotPlayer = mock(Player.Spigot::class.java)
        config = mock()

        whenever(player.spigot()).thenReturn(spigotPlayer)
    }

    @Test
    fun `sends welcome message set in config`() = runTest {
        val welcomeMessage = "welcome message"
        whenever(config.get()).thenReturn(
            PluginConfig.default.copy(
            messages = PluginConfig.default.messages.copy(
                welcome = welcomeMessage,
            ),
        ))

        val event = PlayerJoinEvent(player, "join message")
        ServerOverviewJoinListener(config).handle(event)

        verify(player.spigot()).sendMessage(
            TextComponent(welcomeMessage)
        )
    }
}

