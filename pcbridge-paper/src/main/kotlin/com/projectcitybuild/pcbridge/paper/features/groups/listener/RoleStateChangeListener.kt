package com.projectcitybuild.pcbridge.paper.features.groups.listener

import com.projectcitybuild.pcbridge.paper.architecture.permissions.Permissions
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RoleStateChangeListener(
    private val permissions: Permissions,
) : Listener {
    @EventHandler
    fun onPlayerStateCreated(event: PlayerStateCreatedEvent) {
        val groupSet = event.state.groups.mapNotNull { it.minecraftName }.toSet()
        permissions.provider.setUserRoles(event.playerUUID, groupSet)
    }

    @EventHandler
    fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        if (event.prevState?.groups == event.state.groups) return

        val groupSet = event.state.groups.mapNotNull { it.minecraftName }.toSet()
        permissions.provider.setUserRoles(event.playerUUID, groupSet)
    }
}
