package com.projectcitybuild.pcbridge.core.store.data

import java.util.UUID

// TODO: allow features to define and watch their own slice
data class Session(
    val players: Map<UUID, PlayerSession> = mapOf(),
)