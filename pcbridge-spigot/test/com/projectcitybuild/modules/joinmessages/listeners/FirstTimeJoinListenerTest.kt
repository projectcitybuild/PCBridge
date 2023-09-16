package com.projectcitybuild.modules.joinmessages.listeners

import com.projectcitybuild.ConfigData
import com.projectcitybuild.events.FirstTimeJoinEvent
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.support.spigot.SpigotServer
import kotlinx.coroutines.test.runTest
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class FirstTimeJoinListenerTest {
    private lateinit var player: Player
    private lateinit var server: SpigotServer
    private lateinit var config: Config<ConfigData>

    @BeforeEach
    fun setUp() {
        player = mock(Player::class.java)
        server = mock(SpigotServer::class.java)
        config = mock()
    }

    @Test
    fun `broadcasts first time join message set in config`() = runTest {
        whenever(player.name).thenReturn("player_name")
        whenever(config.get()).thenReturn(ConfigData.default.copy(
            messages = ConfigData.default.messages.copy(
                firstTimeJoin = "Welcome %name% to the server",
            ),
        ))

        val event = FirstTimeJoinEvent(player)
        FirstTimeJoinListener(
            server,
            config,
            mock(PlatformLogger::class.java),
        ).handle(event)

        verify(server).broadcastMessage(
            TextComponent("Welcome player_name to the server")
        )
    }
}

