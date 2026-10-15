package com.projectcitybuild.pcbridge.features.roles.hooks.listener

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Role
import com.projectcitybuild.pcbridge.runtime.listeners.scopedSync
import com.projectcitybuild.pcbridge.runtime.permissions.Permissions
import com.projectcitybuild.pcbridge.runtime.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.runtime.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.features.roles.rolesTracer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.UUID

class RoleStateChangeListener(
    private val permissions: Permissions,
) : Listener {
    @EventHandler
    fun onPlayerStateCreated(
        event: PlayerStateCreatedEvent,
    ) = event.scopedSync(rolesTracer, this::class.java) {
        val synced = event.state.syncedValue
        if (synced != null) {
            update(event.playerUUID, roles = synced.roles)
        }
    }

    @EventHandler
    fun onPlayerStateUpdated(
        event: PlayerStateUpdatedEvent,
    ) = event.scopedSync(rolesTracer, this::class.java) {
        if (event.prevState?.syncedValue?.roles == event.state.syncedValue?.roles) {
            return@scopedSync
        }
        update(event.playerUUID, roles = event.state.syncedValue!!.roles)
    }

    private fun update(playerUUID: UUID, roles: List<Role>) {
        val roleSet = roles.mapNotNull { it.minecraftName }.toSet()
        permissions.provider.setUserRoles(playerUUID, roleSet)
    }
}
