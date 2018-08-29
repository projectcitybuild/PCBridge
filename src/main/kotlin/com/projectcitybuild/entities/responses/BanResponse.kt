package com.projectcitybuild.entities.responses

data class BanResponse(val playerUuid: String,
                       val playerAlias: String,
                       val staffUuid: String,
                       val serverId: Int,
                       val createdAt: Int,
                       val updatedAt: Int,
                       val isActive: Boolean)