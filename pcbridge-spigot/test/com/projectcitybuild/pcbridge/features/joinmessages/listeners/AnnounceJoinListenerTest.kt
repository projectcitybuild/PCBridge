package com.projectcitybuild.pcbridge.features.joinmessages.listeners

import com.projectcitybuild.pcbridge.data.PluginConfig
import com.projectcitybuild.features.joinmessages.PlayerJoinTimeCache
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.pcbridge.features.joinmessages.listeners.AnnounceJoinListener
import com.projectcitybuild.support.spigot.SpigotServer
import kotlinx.coroutines.test.runTest
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.util.*

class AnnounceJoinListenerTest {
    private lateinit var player: Player
    private lateinit var server: SpigotServer
    private lateinit var playerJoinTimeCache: PlayerJoinTimeCache
    private lateinit var config: Config<PluginConfig>

    @BeforeEach
    fun setUp() {
        player = mock(Player::class.java)
        server = mock(SpigotServer::class.java)
        playerJoinTimeCache = mock(PlayerJoinTimeCache::class.java)
        config = mock()
    }

    @Test
    fun `broadcasts join message set in config`() = runTest {
        whenever(player.name).thenReturn("player_name")
        whenever(player.uniqueId).thenReturn(UUID.randomUUID())

        whenever(config.get()).thenReturn(
            PluginConfig.default.copy(
            messages = PluginConfig.default.messages.copy(
                join = "%name% joined the server",
            ),
        ))

        val event = PlayerJoinEvent(player, "join message")
        AnnounceJoinListener(
            server,
            config,
            playerJoinTimeCache,
        ).handle(event)

        verify(server).broadcastMessage(
            TextComponent("player_name joined the server")
        )
    }
}

