package com.projectcitybuild.pcbridge.http.pcb

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.projectcitybuild.pcbridge.http.shared.logging.StructuredLoggingInterceptor
import com.projectcitybuild.pcbridge.http.shared.serialization.gson.InstantTypeAdapter
import com.projectcitybuild.pcbridge.http.shared.serialization.gson.LocalDateTimeTypeAdapter
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTelemetry
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

internal class PCBClientFactory(
    private val authToken: String,
    private val baseUrl: String,
    private val logger: StructuredLoggingInterceptor?,
    private val openTelemetry: OpenTelemetry,
) {
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter())
        .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    fun build(): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .callFactory(makeTracedClient())
        .build()

    private fun makeTracedClient() = OkHttpTelemetry
        .builder(openTelemetry)
        .build()
        .newCallFactory(makeAuthenticatedClient())

    private fun makeAuthenticatedClient(): OkHttpClient {
        val clientFactory = OkHttpClient()
            .newBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .header("Authorization", "Bearer $authToken")
                    .header("Accept", "application/json")
                    .build()

                chain.proceed(request)
            }

        if (logger != null) {
            clientFactory.addInterceptor(logger)
        }
        return clientFactory.build()
    }
}
