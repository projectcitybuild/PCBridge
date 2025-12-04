package com.projectcitybuild.pcbridge.paper.architecture.state.data

import java.util.UUID

data class Session(
    val players: Map<UUID, PlayerSession> = mapOf(),
)