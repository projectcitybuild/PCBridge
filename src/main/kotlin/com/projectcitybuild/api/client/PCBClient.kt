package com.projectcitybuild.api.client

import com.projectcitybuild.api.interfaces.BanApiInterface
import com.projectcitybuild.api.interfaces.AuthApiInterface
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PCBClient(private val authToken: String,
                private val baseUrl: String,
                withLogging: Boolean) {

    val instance: Retrofit = build(withLogging)

    val banApi: BanApiInterface = instance.create(BanApiInterface::class.java)
    val authApi: AuthApiInterface = instance.create(AuthApiInterface::class.java)

    private fun build(withLogging: Boolean) : Retrofit {
        val authenticatedClient = makeAuthenticatedClient(authToken, withLogging)
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(authenticatedClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    private fun makeAuthenticatedClient(token: String, withLogging: Boolean) : OkHttpClient {
        var clientFactory = OkHttpClient().newBuilder()
                .addInterceptor { chain ->
                    // Add access token as header to each API request
                    val request = chain.request()
                    val requestBuilder = request.newBuilder().header("Authorization", "Bearer $token")
                    val nextRequest = requestBuilder.build()

                    chain.proceed(nextRequest)
                }

        if (withLogging) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            clientFactory = clientFactory.addInterceptor(loggingInterceptor)
        }

        return clientFactory.build()
    }
}