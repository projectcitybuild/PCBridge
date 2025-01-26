package com.projectcitybuild.pcbridge.http.discord.models

import kotlinx.serialization.Serializable

@Serializable
data class DiscordFieldEmbed(
    val name: String?,
    val value: String?,
    val inline: Boolean? = null,
)