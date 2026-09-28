package com.projectcitybuild.pcbridge.paper.features.announcements.domain.data

import com.projectcitybuild.pcbridge.paper.runtime.state.store.featureStateKey

/**
 * The announcements feature's own slice of persisted [Store] state.
 */
data class AnnouncementState(
    val lastBroadcastIndex: Int = 0,
)

val announcementStateKey = featureStateKey("announcements", AnnouncementState())
