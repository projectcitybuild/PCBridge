package com.projectcitybuild.modules.joinmessages.listeners

import com.projectcitybuild.entities.ConfigData
import com.projectcitybuild.modules.joinmessages.PlayerJoinTimeCache
import com.projectcitybuild.pcbridge.core.modules.config.Config
import com.projectcitybuild.pcbridge.core.modules.datetime.time.Time
import com.projectcitybuild.support.spigot.SpigotServer
import kotlinx.coroutines.test.runTest
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.UUID

class AnnounceQuitListenerTest {
    private lateinit var player: Player
    private lateinit var server: SpigotServer
    private lateinit var playerJoinTimeCache: PlayerJoinTimeCache
    private lateinit var config: Config<ConfigData>
    private lateinit var time: Time

    @BeforeEach
    fun setUp() {
        player = mock(Player::class.java)
        server = mock(SpigotServer::class.java)
        playerJoinTimeCache = mock(PlayerJoinTimeCache::class.java)
        config = mock()
        time = mock(Time::class.java)
    }

    @Test
    fun `broadcasts quit message set in config`() = runTest {
        val uuid = UUID.randomUUID()
        val joinTime = LocalDateTime.of(2023, 9, 16, 0, 0)
        val now = LocalDateTime.of(2023, 9, 16, 1, 30)

        whenever(time.now()).thenReturn(now)
        whenever(player.name).thenReturn("player_name")
        whenever(player.uniqueId).thenReturn(uuid)
        whenever(playerJoinTimeCache.get(uuid)).thenReturn(joinTime)
        whenever(config.get()).thenReturn(ConfigData.default.copy(
            messages = ConfigData.default.messages.copy(
                leave = "%name% left the server (online for %time_online%)",
            ),
        ))

        val event = PlayerQuitEvent(player, "quit message")
        AnnounceQuitListener(
            server,
            config,
            playerJoinTimeCache,
            time,
        ).handle(event)

        verify(server).broadcastMessage(
            TextComponent("player_name left the server (online for 1 hour)")
        )
    }


    @Test
    fun `shows highest time unit for connection time`() = runTest {
        listOf(
            Pair(
                LocalDateTime.of(2023, 9, 17, 2, 0),
                "26 hours",
            ),
            Pair(
                LocalDateTime.of(2023, 9, 16, 2, 15),
                "2 hours",
            ),
            Pair(
                LocalDateTime.of(2023, 9, 16, 1, 30),
                "1 hour",
            ),
            Pair(
                LocalDateTime.of(2023, 9, 16, 0, 30),
                "30 mins",
            ),
            Pair(
                LocalDateTime.of(2023, 9, 16, 0, 1),
                "1 min",
            ),
            Pair(
                LocalDateTime.of(2023, 9, 16, 0, 0, 30),
                "30 secs",
            ),
            Pair(
                LocalDateTime.of(2023, 9, 16, 0, 0, 1),
                "1 sec",
            ),
        ).forEach { pair ->
            val now = pair.first
            val expectedTime = pair.second
            val uuid = UUID.randomUUID()

            val joinTime = LocalDateTime.of(2023, 9, 16, 0, 0)
            whenever(time.now()).thenReturn(now)
            whenever(player.name).thenReturn("player_name")
            whenever(player.uniqueId).thenReturn(uuid)
            whenever(playerJoinTimeCache.get(uuid)).thenReturn(joinTime)
            whenever(config.get()).thenReturn(ConfigData.default.copy(
                messages = ConfigData.default.messages.copy(
                    leave = "%name% left the server (online for %time_online%)",
                ),
            ))

            val event = PlayerQuitEvent(player, "quit message")
            AnnounceQuitListener(
                server,
                config,
                playerJoinTimeCache,
                time,
            ).handle(event)

            verify(server).broadcastMessage(
                TextComponent("player_name left the server (online for $expectedTime)")
            )
        }
    }
}

