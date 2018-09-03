package com.projectcitybuild.entities.models

data class GameBan(val id: Int,
                   val serverId: Int,
                   val playerId: String,
                   val playerType: String,
                   val playerAlias: String,
                   val staffId: String?,
                   val staffType: String,
                   val reason: String,
                   val isActive: Boolean,
                   val isGlobalBan: Boolean,
                   val createdAt: Long,
                   val updatedAt: Long,
                   val expiresAt: Long?)
