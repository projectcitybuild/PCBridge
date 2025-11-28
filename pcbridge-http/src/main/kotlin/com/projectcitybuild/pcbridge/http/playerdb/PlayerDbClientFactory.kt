package com.projectcitybuild.pcbridge.http.playerdb

import com.google.gson.GsonBuilder
import com.projectcitybuild.pcbridge.http.shared.logging.HttpLogger
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTelemetry
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class PlayerDbClientFactory(
    private val baseUrl: String,
    private val httpLogger: HttpLogger?,
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
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .header("Accept", "application/json")
                    .header("user-agent", userAgent)
                    .build()

                chain.proceed(request)
            }
        if (httpLogger != null) {
            val loggingInterceptor = HttpLoggingInterceptor(httpLogger).apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            clientFactory.addInterceptor(loggingInterceptor)
        }
        return clientFactory.build()
    }
}
