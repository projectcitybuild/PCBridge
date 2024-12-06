package com.projectcitybuild.pcbridge.http.discord

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class DiscordClientFactory(
    private val withLogging: Boolean,
) {
    fun build(): Retrofit = Retrofit.Builder()
        .baseUrl("https://discord.com/api/")
        .client(makeClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private fun makeClient(): OkHttpClient {
        return OkHttpClient().newBuilder().run {
            if (withLogging) {
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

                addInterceptor(loggingInterceptor)
            }
            build()
        }
    }
}
