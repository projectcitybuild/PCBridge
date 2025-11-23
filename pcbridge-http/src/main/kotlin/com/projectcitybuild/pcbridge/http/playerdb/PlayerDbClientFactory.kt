package com.projectcitybuild.pcbridge.http.playerdb

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class PlayerDbClientFactory(
    private val baseUrl: String,
    private val withLogging: Boolean,
    private val userAgent: String,
) {
    private val gson = GsonBuilder().create()

    fun build(): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(makeClient())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private fun makeClient(): OkHttpClient {
        var clientFactory =
            OkHttpClient().newBuilder()
                .addInterceptor { chain ->
                    val request = chain.request()
                        .newBuilder()
                        .header("Accept", "application/json")
                        .header("user-agent", userAgent)
                        .build()

                    chain.proceed(request)
                }
        if (withLogging) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            clientFactory = clientFactory.addInterceptor(loggingInterceptor)
        }
        return clientFactory.build()
    }
}
