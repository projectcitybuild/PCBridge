package com.projectcitybuild.pcbridge.features.onboarding.hooks.listeners

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.runtime.state.data.PlayerSession
import com.projectcitybuild.pcbridge.runtime.state.data.PlayerSyncedState
import com.projectcitybuild.pcbridge.runtime.state.store.SessionStore
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.UUID
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Player as ApiPlayer

class FirstTimeJoinListenerTest {
    private val server = mock(Server::class.java)
    private val session = SessionStore()
    private val remoteConfig = mock(RemoteConfig::class.java)
    private val listener = FirstTimeJoinListener(server, session, remoteConfig)

    @Test
    fun `welcomes other online players for a first-time join`() =
        runTest {
            val joiningPlayer = mock(Player::class.java)
            val otherPlayer = mock(Player::class.java)
            val id = UUID.randomUUID()
            whenever(joiningPlayer.uniqueId).thenReturn(id)
            whenever(joiningPlayer.name).thenReturn("Alex")
            whenever(otherPlayer.uniqueId).thenReturn(UUID.randomUUID())
            whenever(server.onlinePlayers).thenReturn(setOf(joiningPlayer, otherPlayer))
            session.mutate {
                it.copy(
                    players =
                        mapOf(
                            id to
                                PlayerSession(
                                    PlayerSyncedState.Valid(player = ApiPlayer(id = 1, uuid = id.toString())),
                                    LocalDateTime.parse("2024-01-01T00:00:00"),
                                ),
                        ),
                )
            }
            whenever(remoteConfig.latest).thenReturn(
                RemoteConfigVersion(
                    1,
                    RemoteConfigKeyValues(messages = RemoteConfigKeyValues.Messages(firstTimeJoin = "Welcome <name>")),
                ),
            )
            val event = mock(PlayerJoinEvent::class.java)
            whenever(event.player).thenReturn(joiningPlayer)

            listener.onPlayerJoin(event)

            verify(otherPlayer).sendMessage(MiniMessage.miniMessage().deserialize("Welcome Alex"))
            verify(joiningPlayer, never()).sendMessage(org.mockito.kotlin.any<net.kyori.adventure.text.Component>())
        }

    @Test
    fun `does not announce a returning player`() =
        runTest {
            val player = mock(Player::class.java)
            val id = UUID.randomUUID()
            whenever(player.uniqueId).thenReturn(id)
            whenever(player.name).thenReturn("Alex")
            whenever(server.onlinePlayers).thenReturn(setOf(player))
            session.mutate {
                it.copy(
                    players =
                        mapOf(
                            id to
                                PlayerSession(
                                    PlayerSyncedState.Valid(
                                        player =
                                            ApiPlayer(
                                                id = 1,
                                                uuid = id.toString(),
                                                lastSeenAt = LocalDateTime.parse("2023-12-31T00:00:00"),
                                            ),
                                    ),
                                    LocalDateTime.parse("2024-01-01T00:00:00"),
                                ),
                        ),
                )
            }
            val event = mock(PlayerJoinEvent::class.java)
            whenever(event.player).thenReturn(player)

            listener.onPlayerJoin(event)

            verify(remoteConfig, never()).latest
        }
}
