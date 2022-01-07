package com.projectcitybuild.entities.responses

data class GameUnban(
    val id: Int,
    val gameBan: GameBan,
    val staffId: String,
    val staffType: String
)