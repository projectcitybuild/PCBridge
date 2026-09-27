package com.projectcitybuild.pcbridge.http.pcb.models

data class PlayerStats(
    val afkTime: Long? = null,
    val blocksPlaced: Long? = null,
    val blocksDestroyed: Long? = null,
)

data class PlayersStatsRequest(
    val players: Map<String, PlayerStats>,
)