package com.projectcitybuild.pcbridge.http.discord

import com.projectcitybuild.pcbridge.core.observability.tracing.HttpTracer
import com.projectcitybuild.pcbridge.http.shared.logging.StructuredLoggingInterceptor
import okhttp3.Call
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class DiscordClientFactory(
    private val logger: StructuredLoggingInterceptor?,
    private val httpTracer: HttpTracer,
) {
    fun build(): Retrofit = Retrofit.Builder()
        .baseUrl("https://discord.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .callFactory(makeTracedClient())
        .build()

    private fun makeTracedClient(): Call.Factory {
        val client = makeClient()
        return httpTracer.instrument(client)
    }

    private fun makeClient(): OkHttpClient {
        return OkHttpClient().newBuilder().run {
            if (logger != null) {
                addInterceptor(logger)
            }
            build()
        }
    }
}
