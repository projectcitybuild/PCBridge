package com.projectcitybuild.pcbridge.core.pcbridgeapi.discord.models

import kotlinx.serialization.Serializable

@Serializable
data class DiscordThumbnailEmbed(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)