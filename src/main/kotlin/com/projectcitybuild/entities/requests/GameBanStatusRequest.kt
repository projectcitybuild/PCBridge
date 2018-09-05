package com.projectcitybuild.entities.requests

import retrofit2.http.Field

data class GameBanStatusRequest(
        @Field("player_id") val playerId: String,
        @Field("player_id_type") val playerType: String
)