package com.projectcitybuild.entities.requests

data class GameBanRequest(val playerId: String,
                          val playerType: String,
                          val playerAlias: String,
                          val staffId: String?,
                          val staffType: String,
                          val reason: String,
                          val isGlobalBan: Boolean)
