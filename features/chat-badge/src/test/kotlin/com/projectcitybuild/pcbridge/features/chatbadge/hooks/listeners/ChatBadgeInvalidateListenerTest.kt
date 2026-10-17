package com.projectcitybuild.pcbridge.features.chatbadge.hooks.listeners

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Badge
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigKeyValues
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.features.chatbadge.domain.repositories.ChatBadgeRepository
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfigUpdatedEvent
import com.projectcitybuild.pcbridge.runtime.state.data.PlayerSession
import com.projectcitybuild.pcbridge.runtime.state.data.PlayerSyncedState
import com.projectcitybuild.pcbridge.runtime.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.runtime.state.events.PlayerStateUpdatedEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.time.LocalDateTime
import java.util.UUID

class ChatBadgeInvalidateListenerTest {
    private val repository = mock(ChatBadgeRepository::class.java)
    private val listener = ChatBadgeInvalidateListener(repository)

    @Test
    fun `rebuilds all badges when the configured icon changes`() {
        listener.onRemoteConfigUpdated(configEvent(previousIcon = "★", nextIcon = "◆"))

        verify(repository).invalidateAll()
    }

    @Test
    fun `rebuilds all badges when no previous config exists`() {
        listener.onRemoteConfigUpdated(
            RemoteConfigUpdatedEvent(
                prev = null,
                next = RemoteConfigVersion(version = 1, config = RemoteConfigKeyValues()),
            ),
        )

        verify(repository).invalidateAll()
    }

    @Test
    fun `keeps cached badges when the configured icon is unchanged`() {
        listener.onRemoteConfigUpdated(configEvent(previousIcon = "★", nextIcon = "★"))

        verify(repository, never()).invalidateAll()
    }

    @Test
    fun `invalidates the created player's cached badges`() {
        val playerUuid = UUID.randomUUID()

        listener.onPlayerStateCreated(
            PlayerStateCreatedEvent(playerSession(emptyList()), playerUuid),
        )

        verify(repository).invalidate(playerUuid)
    }

    @Test
    fun `invalidates badges when a player's badge list changes`() {
        val playerUuid = UUID.randomUUID()

        listener.onPlayerStateUpdated(
            PlayerStateUpdatedEvent(
                prevState = playerSession(emptyList()),
                state = playerSession(listOf(com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Badge("Builder", "◆"))),
                playerUUID = playerUuid,
            ),
        )

        verify(repository).invalidate(playerUuid)
    }

    @Test
    fun `keeps cached badges when a player's badge list is unchanged`() {
        val playerUuid = UUID.randomUUID()
        val badges = listOf(com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Badge("Builder", "◆"))

        listener.onPlayerStateUpdated(
            PlayerStateUpdatedEvent(
                prevState = playerSession(badges),
                state = playerSession(badges),
                playerUUID = playerUuid,
            ),
        )

        verify(repository, never()).invalidate(playerUuid)
    }

    private fun configEvent(
        previousIcon: String,
        nextIcon: String,
    ) = RemoteConfigUpdatedEvent(
        prev =
            RemoteConfigVersion(
                version = 1,
                config = RemoteConfigKeyValues(chat = RemoteConfigKeyValues.Chat(badgeIcon = previousIcon)),
            ),
        next =
            RemoteConfigVersion(
                version = 2,
                config = RemoteConfigKeyValues(chat = RemoteConfigKeyValues.Chat(badgeIcon = nextIcon)),
            ),
    )

    private fun playerSession(badges: List<Badge>) =
        PlayerSession(
            synced = PlayerSyncedState.Valid(badges = badges),
            connectedAt = LocalDateTime.MIN,
        )
}
