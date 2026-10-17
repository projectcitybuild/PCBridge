package com.projectcitybuild.pcbridge.runtime.teleportation.storage

import com.projectcitybuild.pcbridge.platform.paper.serialization.SerializableLocation
import com.projectcitybuild.pcbridge.runtime.state.store.featureStateKey

data class TeleportHistoryState(
    val previousLocations: Map<String, SerializableLocation> = emptyMap(),
)

val teleportHistoryStateKey = featureStateKey(
    name = "teleport-history",
    default = TeleportHistoryState(),
)
