package com.projectcitybuild.core.entities.models

data class GameUnban(
    val id: Int,
    val gameBan: GameBan,
    val staffId: String,
    val staffType: String
)