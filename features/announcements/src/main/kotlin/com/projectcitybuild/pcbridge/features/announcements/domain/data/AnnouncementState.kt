package com.projectcitybuild.pcbridge.features.announcements.domain.data

import com.projectcitybuild.pcbridge.runtime.state.store.featureStateKey

/**
 * The announcements feature's own slice of persisted [Store] state.
 */
data class AnnouncementState(
    val lastBroadcastIndex: Int = 0,
)

val announcementStateKey = featureStateKey("announcements", AnnouncementState())
