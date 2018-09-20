package com.projectcitybuild.api.client

import com.projectcitybuild.api.interfaces.MojangApiInterface
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MojangClient {

    private val instance: Retrofit = build()
    private val client: OkHttpClient
        get() {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            return OkHttpClient().newBuilder()
                    .addInterceptor(interceptor)
                    .build()
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