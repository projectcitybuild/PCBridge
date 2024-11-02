package com.projectcitybuild.pcbridge.http.models.discord

import kotlinx.serialization.Serializable

@Serializable
data class DiscordWebhookBody(
    val content: String? = null,
    val embeds: List<DiscordEmbed>? = null,
)