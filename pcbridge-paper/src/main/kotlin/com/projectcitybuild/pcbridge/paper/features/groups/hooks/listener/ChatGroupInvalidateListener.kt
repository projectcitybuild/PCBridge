package com.projectcitybuild.pcbridge.paper.features.groups.hooks.listener

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.groups.domain.repositories.ChatGroupRepository
import com.projectcitybuild.pcbridge.paper.features.groups.groupsTracer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatGroupInvalidateListener(
    private val chatGroupRepository: ChatGroupRepository,
) : Listener {
    @EventHandler
    fun onPlayerStateCreated(
        event: PlayerStateCreatedEvent,
    ) = event.scopedSync(groupsTracer, this::class.java) {
        chatGroupRepository.invalidate(event.playerUUID)
    }

    @EventHandler
    fun onPlayerStateUpdated(
        event: PlayerStateUpdatedEvent,
    ) = event.scopedSync(groupsTracer, this::class.java) {
        if (event.prevState?.syncedValue?.roles == event.state.syncedValue?.roles) {
            return@scopedSync
        }
        logSync.info { "Invalidating chat group cache for ${event.playerUUID}" }
        chatGroupRepository.invalidate(event.playerUUID)
    }
}
