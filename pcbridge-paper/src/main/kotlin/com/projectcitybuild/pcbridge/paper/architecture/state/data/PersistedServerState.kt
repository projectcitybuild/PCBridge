package com.projectcitybuild.pcbridge.paper.architecture.state.data

data class PersistedServerState(
    val lastBroadcastIndex: Int,
    val maintenance: Boolean,
) {
    fun toServerState() = ServerState.default().copy(
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