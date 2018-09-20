package com.projectcitybuild.api.client

import com.projectcitybuild.api.interfaces.BanApiInterface
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PCBClient(private val authToken: String,
                private val baseUrl: String) {

    private val instance: Retrofit = build()

    val banApi = instance.create(BanApiInterface::class.java)

    private fun build() : Retrofit {
        val authenticatedClient = makeAuthenticatedClient(authToken)
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(authenticatedClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    private fun makeAuthenticatedClient(token: String) : OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val clientFactory = OkHttpClient().newBuilder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    val requestBuilder = request.newBuilder().header("Authorization", "Bearer $token")
                    val nextRequest = requestBuilder.build()

                    chain.proceed(nextRequest)
                }
                .addInterceptor(interceptor)

        return clientFactory.build()
    }
}