package com.projectcitybuild.pcbridge.paper.features.announcements.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.config.domain.data.RemoteConfigUpdatedEvent
import com.projectcitybuild.pcbridge.paper.features.announcements.actions.StartAnnouncementTimer
import com.projectcitybuild.pcbridge.paper.features.announcements.announcementsTracer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class AnnouncementConfigListener(
    private val announcementTimer: StartAnnouncementTimer,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onRemoteConfigUpdated(
        event: RemoteConfigUpdatedEvent,
    ) = event.scopedSync(announcementsTracer, this::class.java) {
        val prev = event.prev?.config
        val next = event.next.config

        if (prev?.announcements == next.announcements) {
            return@scopedSync
        }

        logSync.info { "Announcement config updated. Restarting announcements" }

        // Restart the timer so that the new remote config values are picked up
        announcementTimer.stop()
        announcementTimer.start()
    }
}
