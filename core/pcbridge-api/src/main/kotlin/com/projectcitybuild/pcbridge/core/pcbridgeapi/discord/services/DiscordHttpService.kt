package com.projectcitybuild.pcbridge.core.pcbridgeapi.discord.services

import com.projectcitybuild.pcbridge.core.pcbridgeapi.discord.models.DiscordEmbed
import com.projectcitybuild.pcbridge.core.pcbridgeapi.discord.models.DiscordWebhookBody
import com.projectcitybuild.pcbridge.core.pcbridgeapi.discord.requests.discord
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
