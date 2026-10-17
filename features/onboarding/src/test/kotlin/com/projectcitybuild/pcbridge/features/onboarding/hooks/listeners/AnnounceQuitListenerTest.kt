package com.projectcitybuild.pcbridge.features.onboarding.hooks.listeners

import com.projectcitybuild.pcbridge.core.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.runtime.state.data.PlayerSession
import com.projectcitybuild.pcbridge.runtime.state.data.PlayerSyncedState
import com.projectcitybuild.pcbridge.runtime.state.store.SessionStore
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class AnnounceQuitListenerTest {
    private val remoteConfig = mock(RemoteConfig::class.java)
    private val session = SessionStore()
    private val clock = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), ZoneOffset.UTC)
    private val listener = AnnounceQuitListener(remoteConfig, session, LocalizedTime(clock))

    @Test
    fun `announces online duration from player session`() =
        runTest {
            val player = mock(Player::class.java)
            val id = UUID.randomUUID()
            val event = mock(PlayerQuitEvent::class.java)
            whenever(player.uniqueId).thenReturn(id)
            whenever(player.name).thenReturn("Alex")
            whenever(event.player).thenReturn(player)
            session.mutate {
                it.copy(
                    players =
                        mapOf(
                            id to PlayerSession(PlayerSyncedState.Unavailable, LocalDateTime.parse("2024-01-01T11:58:00")),
                        ),
                )
            }
            whenever(remoteConfig.latest).thenReturn(
                RemoteConfigVersion(
                    1,
                    RemoteConfigKeyValues(messages = RemoteConfigKeyValues.Messages(leave = "<name>: <time_online>")),
                ),
            )

            listener.onPlayerQuit(event)

            verify(event).quitMessage(MiniMessage.miniMessage().deserialize("Alex: 2 minutes"))
        }
}
