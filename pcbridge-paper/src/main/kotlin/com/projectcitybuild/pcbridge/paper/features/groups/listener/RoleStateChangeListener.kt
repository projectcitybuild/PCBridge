package com.projectcitybuild.pcbridge.paper.features.groups.listener

import com.projectcitybuild.pcbridge.paper.architecture.permissions.Permissions
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RoleStateChangeListener(
    private val permissions: Permissions,
) : Listener {
    @EventHandler
    fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        val groupSet = event.state.groups.mapNotNull { it.minecraftName }.toSet()
        permissions.provider.setUserRoles(event.playerUUID, groupSet)
    }
}
