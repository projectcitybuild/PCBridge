package com.projectcitybuild.pcbridge.http.models.discord

import kotlinx.serialization.Serializable

@Serializable
data class DiscordEmbed(
    val title: String? = null,
    val type: String = "rich", // Always "rich" for webhook embeds
    val description: String? = null,
    val timestamp: String? = null,
    val color: Int? = null,
    val author: DiscordAuthorEmbed? = null,
    val thumbnail: DiscordThumbnailEmbed? = null,
    val fields: List<DiscordFieldEmbed>? = null,
)