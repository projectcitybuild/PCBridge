package com.projectcitybuild.pcbridge.paper.features.announcements.repositories

import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.services.RemoteConfig
import com.projectcitybuild.pcbridge.paper.architecture.store.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnnouncementRepository(
    private val remoteConfig: RemoteConfig,
    private val store: Store,
) {
    suspend fun getNextAnnouncement(): String =
        withContext(Dispatchers.IO) {
            val config = remoteConfig.latest.config
            val announcements = config.announcements.messages
            val lastBroadcastIndex = store.state.lastBroadcastIndex
            val nextIndex = (lastBroadcastIndex + 1) % announcements.size
            store.mutate { state ->
                state.copy(lastBroadcastIndex = nextIndex)
            }
            announcements[nextIndex]
        }
}
