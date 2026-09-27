package com.projectcitybuild.pcbridge.http.discord

import com.projectcitybuild.pcbridge.http.shared.logging.StructuredLoggingInterceptor
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTelemetry
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class DiscordClientFactory(
    private val logger: StructuredLoggingInterceptor?,
    private val openTelemetry: OpenTelemetry,
) {
    fun build(): Retrofit = Retrofit.Builder()
        .baseUrl("https://discord.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .callFactory(makeTracedClient())
        .build()

    private fun makeTracedClient() = OkHttpTelemetry
        .builder(openTelemetry)
        .build()
        .newCallFactory(makeClient())

    private fun makeClient(): OkHttpClient {
        return OkHttpClient().newBuilder().run {
            if (logger != null) {
                addInterceptor(logger)
            }
            build()
        }
    }
}
