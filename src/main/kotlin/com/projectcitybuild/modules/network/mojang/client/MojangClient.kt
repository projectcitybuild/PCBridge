package com.projectcitybuild.modules.network.mojang.client

import com.projectcitybuild.modules.network.mojang.requests.MojangApiInterface
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MojangClient(private val withLogging: Boolean) {

    private val instance: Retrofit = build()
    private val client: OkHttpClient
        get() {
            var clientBuilder = OkHttpClient().newBuilder()

            if (withLogging) {
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

                clientBuilder = clientBuilder.addInterceptor(loggingInterceptor)
            }

            return clientBuilder.build()
        }

    val mojangApi = instance.create(MojangApiInterface::class.java)

    private fun build() : Retrofit {
        return Retrofit.Builder()
                .client(client)
                .baseUrl("https://api.mojang.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }
}