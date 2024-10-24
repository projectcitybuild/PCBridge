package com.projectcitybuild.pcbridge.http.clients


import com.google.gson.GsonBuilder
import com.projectcitybuild.pcbridge.http.serialization.gson.LocalDateTimeTypeAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime

internal class PCBClientFactory(
    private val authToken: String,
    private val baseUrl: String,
    private val withLogging: Boolean,
) {
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter())
        .create()

    fun build(): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(makeAuthenticatedClient())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private fun makeAuthenticatedClient(): OkHttpClient {
        var clientFactory =
            OkHttpClient().newBuilder()
                .addInterceptor { chain ->
                    val request = chain.request()
                        .newBuilder()
                        .header("Authorization", "Bearer $authToken")
                        .header("Accept", "application/json")
                        .build()

                    chain.proceed(request)
                }

        if (withLogging) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            clientFactory = clientFactory.addInterceptor(loggingInterceptor)
        }

        return clientFactory.build()
    }
}
