package com.projectcitybuild.pcbridge.http.services.discord

import com.projectcitybuild.pcbridge.http.requests.discord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

class DiscordHttpService(
    private val retrofit: Retrofit,
) {
    suspend fun executeWebhook(
        webhookUrl: String,
        content: String,
    ) = withContext(Dispatchers.IO) {
        retrofit.discord().executeWebhook(webhookUrl, content)
    }
}
