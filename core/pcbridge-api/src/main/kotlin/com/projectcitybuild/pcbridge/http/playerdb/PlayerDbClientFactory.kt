package com.projectcitybuild.pcbridge.http.playerdb

import com.google.gson.GsonBuilder
import com.projectcitybuild.pcbridge.http.shared.logging.StructuredLoggingInterceptor
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTelemetry
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

internal class PlayerDbClientFactory(
    private val baseUrl: String,
    private val logger: StructuredLoggingInterceptor?,
    private val openTelemetry: OpenTelemetry,
    private val userAgent: String,
) {
    private val gson = GsonBuilder().create()

    fun build(): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .callFactory(makeTracedClient())
        .build()

    private fun makeTracedClient() = OkHttpTelemetry
        .builder(openTelemetry)
        .build()
        .newCallFactory(makeClient())

    private fun makeClient(): OkHttpClient {
        val clientFactory = OkHttpClient()
            .newBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .header("Accept", "application/json")
                    .header("user-agent", userAgent)
                    .build()

                chain.proceed(request)
            }
        if (logger != null) {
            clientFactory.addInterceptor(logger)
        }
        return clientFactory.build()
    }
}
