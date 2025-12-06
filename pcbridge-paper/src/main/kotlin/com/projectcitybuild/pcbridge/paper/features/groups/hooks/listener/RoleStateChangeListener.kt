package com.projectcitybuild.pcbridge.paper.features.groups.hooks.listener

import com.projectcitybuild.pcbridge.http.pcb.models.Group
import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.architecture.permissions.Permissions
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.features.groups.groupsTracer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.UUID

class RoleStateChangeListener(
    private val permissions: Permissions,
) : Listener {
    @EventHandler
    fun onPlayerStateCreated(
        event: PlayerStateCreatedEvent,
    ) = event.scopedSync(groupsTracer, this::class.java) {
        val synced = event.state.syncedValue
        if (synced != null) {
            update(event.playerUUID, groups = synced.groups)
        }
    }

    @EventHandler
    fun onPlayerStateUpdated(
        event: PlayerStateUpdatedEvent,
    ) = event.scopedSync(groupsTracer, this::class.java) {
        if (event.prevState?.syncedValue?.groups == event.state.syncedValue?.groups) {
            return@scopedSync
        }
        update(event.playerUUID, groups = event.state.syncedValue!!.groups)
    }

    private fun update(playerUUID: UUID, groups: List<Group>) {
        val groupSet = groups.mapNotNull { it.minecraftName }.toSet()
        permissions.provider.setUserRoles(playerUUID, groupSet)
    }
}
