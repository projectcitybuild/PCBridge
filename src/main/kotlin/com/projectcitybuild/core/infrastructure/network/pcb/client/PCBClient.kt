package com.projectcitybuild.core.infrastructure.network.pcb.client

import com.projectcitybuild.core.infrastructure.network.pcb.requests.AuthApiInterface
import com.projectcitybuild.core.infrastructure.network.pcb.requests.BalanceApiInterface
import com.projectcitybuild.core.infrastructure.network.pcb.requests.BanApiInterface
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PCBClient(
    private val oldAuthToken: String,  // Deprecated
    private val authToken: String,
    private val baseUrl: String,
    private val withLogging: Boolean
) {
    private val instance: Retrofit = build()

    val banApi: BanApiInterface = instance.create(BanApiInterface::class.java)
    val authApi: AuthApiInterface = instance.create(AuthApiInterface::class.java)
    val balanceApi: BalanceApiInterface = instance.create(BalanceApiInterface::class.java)

    private fun build(): Retrofit {
        val authenticatedClient = makeAuthenticatedClient()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(authenticatedClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun makeAuthenticatedClient(): OkHttpClient {
        var clientFactory = OkHttpClient().newBuilder()
            .addInterceptor { chain ->
                // Add access token as header to each API request
                val request = chain.request()

                val token = if (request.url.toString().contains("balance")) authToken else oldAuthToken
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
