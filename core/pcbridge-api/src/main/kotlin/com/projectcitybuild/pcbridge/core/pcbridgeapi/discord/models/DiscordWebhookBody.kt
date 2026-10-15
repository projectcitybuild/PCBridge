package com.projectcitybuild.pcbridge.core.pcbridgeapi.discord.models

import kotlinx.serialization.Serializable

@Serializable
data class DiscordWebhookBody(
    val content: String? = null,
    val embeds: List<DiscordEmbed>? = null,
)