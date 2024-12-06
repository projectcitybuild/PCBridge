package com.projectcitybuild.pcbridge.http.discord.models

import kotlinx.serialization.Serializable

@Serializable
data class DiscordThumbnailEmbed(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)