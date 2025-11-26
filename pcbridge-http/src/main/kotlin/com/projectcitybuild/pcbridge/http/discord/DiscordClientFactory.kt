package com.projectcitybuild.pcbridge.http.discord

import com.projectcitybuild.pcbridge.http.shared.logging.HttpLogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class DiscordClientFactory(
    private val httpLogger: HttpLogger?,
) {
    fun build(): Retrofit = Retrofit.Builder()
        .baseUrl("https://discord.com/api/")
        .client(makeClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

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
