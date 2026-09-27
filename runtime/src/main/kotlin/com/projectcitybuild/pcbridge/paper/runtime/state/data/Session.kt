package com.projectcitybuild.pcbridge.paper.runtime.state.data

import java.util.UUID

data class Session(
    val players: Map<UUID, PlayerSession> = mapOf(),
)
