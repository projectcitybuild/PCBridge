package com.projectcitybuild.pcbridge.http.requests

import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

internal fun Retrofit.discord() = create(DiscordRequest::class.java)

internal interface DiscordRequest {
    @POST("webhooks/{webhook_url}")
    @FormUrlEncoded
    suspend fun executeWebhook(
        @Path("webhook_url") webhookUrl: String,
        @Field(value = "content") content: String,
    )
}
