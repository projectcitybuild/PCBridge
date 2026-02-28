package com.projectcitybuild.pcbridge.paper.features.roles.hooks.listener

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.roles.domain.repositories.ChatRoleRepository
import com.projectcitybuild.pcbridge.paper.features.roles.rolesTracer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatRoleInvalidateListener(
    private val chatRoleRepository: ChatRoleRepository,
) : Listener {
    @EventHandler
    fun onPlayerStateCreated(
        event: PlayerStateCreatedEvent,
    ) = event.scopedSync(rolesTracer, this::class.java) {
        chatRoleRepository.invalidate(event.playerUUID)
    }

    @EventHandler
    fun onPlayerStateUpdated(
        event: PlayerStateUpdatedEvent,
    ) = event.scopedSync(rolesTracer, this::class.java) {
        if (event.prevState?.syncedValue?.roles == event.state.syncedValue?.roles) {
            return@scopedSync
        }
        logSync.info { "Invalidating chat role cache for ${event.playerUUID}" }
        chatRoleRepository.invalidate(event.playerUUID)
    }
}
