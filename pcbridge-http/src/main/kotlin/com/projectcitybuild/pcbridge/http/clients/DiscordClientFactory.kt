package com.projectcitybuild.pcbridge.http.clients


import com.google.gson.GsonBuilder
import com.projectcitybuild.pcbridge.http.serialization.gson.LocalDateTimeTypeAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime

internal class DiscordClientFactory(
    private val withLogging: Boolean,
) {
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeTypeAdapter())
        .create()

    fun build(): Retrofit = Retrofit.Builder()
        .baseUrl("https://discord.com/api/")
        .client(makeClient())
        .addConverterFactory(GsonConverterFactory.create(gson))
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
