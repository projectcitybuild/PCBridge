package com.projectcitybuild.pcbridge.http.discord.requests

import com.projectcitybuild.pcbridge.http.discord.models.DiscordWebhookBody
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

internal fun Retrofit.discord() = create(DiscordRequest::class.java)

internal interface DiscordRequest {
    @POST("webhooks/{webhook_url}")
    suspend fun executeWebhook(
        @Path("webhook_url") webhookUrl: String,
        @Body request: DiscordWebhookBody,
    )
}
