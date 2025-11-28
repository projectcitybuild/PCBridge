package com.projectcitybuild.pcbridge.http.pcb


import com.google.gson.GsonBuilder
import com.projectcitybuild.pcbridge.http.shared.logging.HttpLogger
import com.projectcitybuild.pcbridge.http.shared.serialization.gson.LocalDateTimeTypeAdapter
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTelemetry
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime

internal class PCBClientFactory(
    private val authToken: String,
    private val baseUrl: String,
    private val httpLogger: HttpLogger?,
    private val openTelemetry: OpenTelemetry,
) {
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter())
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
            .addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .header("Authorization", "Bearer $authToken")
                    .header("Accept", "application/json")
                    .build()

                chain.proceed(request)
            }

        if (httpLogger != null) {
            val loggingInterceptor = HttpLoggingInterceptor(httpLogger).apply {
                level = HttpLoggingInterceptor.Level.BODY
                redactHeader("Authorization")
            }
            clientFactory.addInterceptor(loggingInterceptor)
        }
        return clientFactory.build()
    }
}
