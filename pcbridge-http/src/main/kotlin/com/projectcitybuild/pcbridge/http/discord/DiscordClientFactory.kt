package com.projectcitybuild.pcbridge.http.discord

import com.projectcitybuild.pcbridge.http.shared.logging.HttpLogger
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTelemetry
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class DiscordClientFactory(
    private val httpLogger: HttpLogger?,
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
            if (httpLogger != null) {
                val loggingInterceptor = HttpLoggingInterceptor(httpLogger)
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

                addInterceptor(loggingInterceptor)
            }
            build()
        }
    }
}
