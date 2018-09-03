package com.projectcitybuild.api.client

import com.projectcitybuild.api.interfaces.BanApiInterface
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PCBClient(private val token: String) {
    private val instance: Retrofit = build()

    val banAPI = instance.create(BanApiInterface::class.java)

    private fun build() : Retrofit {
        val authenticatedClient = makeAuthenticatedClient(token)
        return Retrofit.Builder()
                .baseUrl("https://projectcitybuild.com/api")
                .client(authenticatedClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    private fun makeAuthenticatedClient(token: String) : OkHttpClient {
        val clientFactory = OkHttpClient().newBuilder().addInterceptor { chain ->
            val request = chain.request()
            val requestBuilder = request.newBuilder().header("Authorization", token)
            val nextRequest = requestBuilder.build()

            chain.proceed(nextRequest)
        }
        return clientFactory.build()
    }
}