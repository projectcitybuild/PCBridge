package com.projectcitybuild.pcbridge.features.roles.hooks.listener

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Role
import com.projectcitybuild.pcbridge.runtime.permissions.Permissions
import com.projectcitybuild.pcbridge.runtime.permissions.PermissionsProvider
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

class RoleStateChangeListenerTest {
    private val provider = mock(PermissionsProvider::class.java)
    private val permissions =
        Permissions().apply {
            setProvider(this@RoleStateChangeListenerTest.provider)
        }
    private val listener = RoleStateChangeListener(permissions)

    @Test
    fun `loads minecraft role names when player state is created`() {
        val playerUuid = UUID.randomUUID()

        listener.onPlayerStateCreated(
            PlayerStateCreatedEvent(
                playerSession(listOf(role("builder"), role("staff"), roleWithoutMinecraftName())),
                playerUuid,
            ),
        )

        verify(provider).setUserRoles(playerUuid, setOf("builder", "staff"))
    }

    @Test
    fun `does not set roles when created player state is unavailable`() {
        listener.onPlayerStateCreated(
            PlayerStateCreatedEvent(
                PlayerSession(PlayerSyncedState.Unavailable, LocalDateTime.MIN),
                UUID.randomUUID(),
            ),
        )

        verify(provider, never()).setUserRoles(org.mockito.kotlin.any(), org.mockito.kotlin.any())
    }

    @Test
    fun `updates permissions when the role list changes`() {
        val playerUuid = UUID.randomUUID()

        listener.onPlayerStateUpdated(
            PlayerStateUpdatedEvent(
                prevState = playerSession(listOf(role("builder"))),
                state = playerSession(listOf(role("staff"))),
                playerUUID = playerUuid,
            ),
        )

        verify(provider).setUserRoles(playerUuid, setOf("staff"))
    }

    @Test
    fun `updates permissions when the previous state is absent`() {
        val playerUuid = UUID.randomUUID()

        listener.onPlayerStateUpdated(
            PlayerStateUpdatedEvent(
                prevState = null,
                state = playerSession(listOf(role("builder"))),
                playerUUID = playerUuid,
            ),
        )

        verify(provider).setUserRoles(playerUuid, setOf("builder"))
    }

    @Test
    fun `does not update permissions when the role list is unchanged`() {
        val roles = listOf(role("builder"))

        listener.onPlayerStateUpdated(
            PlayerStateUpdatedEvent(
                prevState = playerSession(roles),
                state = playerSession(roles),
                playerUUID = UUID.randomUUID(),
            ),
        )

        verify(provider, never()).setUserRoles(org.mockito.kotlin.any(), org.mockito.kotlin.any())
    }

    private fun playerSession(roles: List<Role>) =
        PlayerSession(
            PlayerSyncedState.Valid(roles = roles),
            LocalDateTime.MIN,
        )

    private fun role(name: String) =
        Role(
            id = name.hashCode(),
            name = name,
            minecraftName = name,
            hoverText = null,
            displayName = name,
            roleType = "build",
            displayPriority = 1,
        )

    private fun roleWithoutMinecraftName() = role("non-minecraft").copy(minecraftName = null)
}
