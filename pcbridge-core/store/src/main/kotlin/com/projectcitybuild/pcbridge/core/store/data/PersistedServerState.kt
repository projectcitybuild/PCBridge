package com.projectcitybuild.pcbridge.core.store.data

// TODO: allow features to define and watch their own slice
data class PersistedServerState(
    val lastBroadcastIndex: Int,
    val maintenance: Boolean,
) {
    fun toServerState() = ServerState(
        lastBroadcastIndex = lastBroadcastIndex,
        maintenance = maintenance,
    )

    companion object {
        fun fromServerState(state: ServerState) = PersistedServerState(
            lastBroadcastIndex = state.lastBroadcastIndex,
            maintenance = state.maintenance,
        )
    }
}