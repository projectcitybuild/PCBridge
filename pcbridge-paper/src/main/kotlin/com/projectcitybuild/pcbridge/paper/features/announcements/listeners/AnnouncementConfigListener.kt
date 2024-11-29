package com.projectcitybuild.pcbridge.paper.features.announcements.listeners

import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.events.RemoteConfigUpdatedEvent
import com.projectcitybuild.pcbridge.paper.features.announcements.actions.StartAnnouncementTimer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class AnnouncementConfigListener(
    private val announcementTimer: StartAnnouncementTimer,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onRemoteConfigUpdated(event: RemoteConfigUpdatedEvent) {
        val prev = event.prev?.config
        val next = event.next.config

        if (prev?.announcements == next.announcements) {
            return
        }

        log.info { "Announcement config updated. Restarting announcements" }

        // Restart the timer so that the new remote config values are picked up
        announcementTimer.stop()
        announcementTimer.start()
    }
}
