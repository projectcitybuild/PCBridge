package com.projectcitybuild.pcbridge.http.services.discord

import com.projectcitybuild.pcbridge.http.models.discord.DiscordEmbed
import com.projectcitybuild.pcbridge.http.models.discord.DiscordWebhookBody
import com.projectcitybuild.pcbridge.http.requests.discord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

class DiscordHttpService(
    private val retrofit: Retrofit,
) {
    suspend fun executeWebhook(
        webhookUrl: String,
        embeds: List<DiscordEmbed>,
    ) = withContext(Dispatchers.IO) {
        retrofit.discord().executeWebhook(
            webhookUrl,
            request = DiscordWebhookBody(
                embeds = embeds,
            )
        )
    }
}
