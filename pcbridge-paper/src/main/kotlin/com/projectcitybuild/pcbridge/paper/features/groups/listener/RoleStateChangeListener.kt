package com.projectcitybuild.pcbridge.paper.features.groups.listener

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
        update(event.playerUUID, groups = event.state.groups)
    }

    @EventHandler
    fun onPlayerStateUpdated(
        event: PlayerStateUpdatedEvent,
    ) = event.scopedSync(groupsTracer, this::class.java) {
        if (event.prevState?.groups == event.state.groups) return@scopedSync

        update(event.playerUUID, groups = event.state.groups)
    }

    private fun update(playerUUID: UUID, groups: List<Group>) {
        val groupSet = groups.mapNotNull { it.minecraftName }.toSet()
        permissions.provider.setUserRoles(playerUUID, groupSet)
    }
}
