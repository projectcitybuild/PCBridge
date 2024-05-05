package com.projectcitybuild.features.announcements.repositories

import com.projectcitybuild.core.state.Store
import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.pcbridge.core.modules.config.Config

class ScheduledAnnouncementsRepository(
    private val config: Config<PluginConfig>,
    private val store: Store,
) {
    suspend fun getNextAnnouncement(): String {
        val announcements = config.get().announcements.messages
        val lastBroadcastIndex = store.state.lastBroadcastIndex
        val nextIndex = (lastBroadcastIndex + 1) % announcements.size
        store.mutate { state ->
            state.copy(lastBroadcastIndex = nextIndex)
        }
        return announcements[nextIndex]
    }
}