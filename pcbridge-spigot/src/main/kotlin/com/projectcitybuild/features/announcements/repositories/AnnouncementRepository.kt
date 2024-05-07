package com.projectcitybuild.features.announcements.repositories

import com.projectcitybuild.core.state.Store
import com.projectcitybuild.data.PluginConfig
import com.projectcitybuild.pcbridge.core.modules.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnnouncementRepository(
    private val config: Config<PluginConfig>,
    private val store: Store,
) {
    suspend fun getNextAnnouncement(): String = withContext(Dispatchers.IO) {
        val announcements = config.get().announcements.messages
        val lastBroadcastIndex = store.state.lastBroadcastIndex
        val nextIndex = (lastBroadcastIndex + 1) % announcements.size
        store.mutate { state ->
            state.copy(lastBroadcastIndex = nextIndex)
        }
        announcements[nextIndex]
    }
}