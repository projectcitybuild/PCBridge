package com.projectcitybuild.pcbridge.http.models.discord

import kotlinx.serialization.Serializable

@Serializable
data class DiscordThumbnailEmbed(
    val url: String? = null,
)