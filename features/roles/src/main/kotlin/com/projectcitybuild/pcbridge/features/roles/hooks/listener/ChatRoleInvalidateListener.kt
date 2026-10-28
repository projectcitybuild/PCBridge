package com.projectcitybuild.pcbridge.features.roles.hooks.listener

import com.projectcitybuild.pcbridge.runtime.listeners.tracedSync
import com.projectcitybuild.pcbridge.runtime.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.runtime.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.features.roles.domain.repositories.ChatRoleRepository
import com.projectcitybuild.pcbridge.features.roles.rolesTracer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatRoleInvalidateListener(
    private val chatRoleRepository: ChatRoleRepository,
) : Listener {
    @EventHandler
    fun onPlayerStateCreated(
        event: PlayerStateCreatedEvent,
    ) = event.tracedSync(rolesTracer, this::class.java) {
        chatRoleRepository.invalidate(event.playerUUID)
    }

    @EventHandler
    fun onPlayerStateUpdated(
        event: PlayerStateUpdatedEvent,
    ) = event.tracedSync(rolesTracer, this::class.java) {
        if (event.prevState?.syncedValue?.roles == event.state.syncedValue?.roles) {
            return@tracedSync
        }
        logSync.info { "Invalidating chat role cache for ${event.playerUUID}" }
        chatRoleRepository.invalidate(event.playerUUID)
    }
}
