package com.projectcitybuild.modules.chat.listeners

import com.projectcitybuild.events.ConnectionPermittedEvent
import com.projectcitybuild.repositories.ChatBadgeRepository
import com.projectcitybuild.support.spigot.listeners.SpigotListener
import org.bukkit.event.EventHandler

class SyncBadgesOnJoinListener(
    private val chatBadgeRepository: ChatBadgeRepository,
): SpigotListener<ConnectionPermittedEvent> {

    @EventHandler
    override suspend fun handle(event: ConnectionPermittedEvent) {
        chatBadgeRepository.put(event.playerUUID, event.aggregate.badges)
    }
}
