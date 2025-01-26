package com.projectcitybuild.pcbridge.http.discord.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordAuthorEmbed(
    val name: String?,
    @SerializedName("icon_url")
    val iconUrl: String? = null,
)