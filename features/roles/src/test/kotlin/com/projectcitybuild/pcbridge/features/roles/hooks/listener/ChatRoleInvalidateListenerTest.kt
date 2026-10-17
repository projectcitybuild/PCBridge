package com.projectcitybuild.pcbridge.features.roles.hooks.listener

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Role
import com.projectcitybuild.pcbridge.features.roles.domain.repositories.ChatRoleRepository
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

class ChatRoleInvalidateListenerTest {
    private val repository = mock(ChatRoleRepository::class.java)
    private val listener = ChatRoleInvalidateListener(repository)

    @Test
    fun `invalidates roles when player state is created`() {
        val playerUuid = UUID.randomUUID()

        listener.onPlayerStateCreated(
            PlayerStateCreatedEvent(
                state = playerSession(emptyList()),
                playerUUID = playerUuid,
            ),
        )

        verify(repository).invalidate(playerUuid)
    }

    @Test
    fun `invalidates roles when the role list changes`() {
        val playerUuid = UUID.randomUUID()
        val previous = playerSession(listOf(role("builder")))
        val current = playerSession(listOf(role("staff")))

        listener.onPlayerStateUpdated(
            PlayerStateUpdatedEvent(
                prevState = previous,
                state = current,
                playerUUID = playerUuid,
            ),
        )

        verify(repository).invalidate(playerUuid)
    }

    @Test
    fun `does not invalidate roles when the role list is unchanged`() {
        val playerUuid = UUID.randomUUID()
        val roles = listOf(role("builder"))

        listener.onPlayerStateUpdated(
            PlayerStateUpdatedEvent(
                prevState = playerSession(roles),
                state = playerSession(roles),
                playerUUID = playerUuid,
            ),
        )

        verify(repository, never()).invalidate(playerUuid)
    }

    @Test
    fun `invalidates roles when an available state arrives without a previous state`() {
        val playerUuid = UUID.randomUUID()

        listener.onPlayerStateUpdated(
            PlayerStateUpdatedEvent(
                prevState = null,
                state = playerSession(listOf(role("builder"))),
                playerUUID = playerUuid,
            ),
        )

        verify(repository).invalidate(playerUuid)
    }

    private fun playerSession(roles: List<Role>) =
        PlayerSession(
            synced = PlayerSyncedState.Valid(roles = roles),
            connectedAt = LocalDateTime.MIN,
        )

    private fun role(name: String) =
        Role(
            id = name.hashCode(),
            name = name,
            minecraftName = null,
            hoverText = null,
            displayName = name,
            roleType = "build",
            displayPriority = 1,
        )
}
