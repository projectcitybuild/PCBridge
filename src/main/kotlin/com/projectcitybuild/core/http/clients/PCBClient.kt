package com.projectcitybuild.core.http.clients

import com.projectcitybuild.entities.requests.pcb.AuthAPIRequest
import com.projectcitybuild.entities.requests.pcb.BalanceAPIRequest
import com.projectcitybuild.entities.requests.pcb.BanAPIRequest
import com.projectcitybuild.entities.requests.pcb.DonorAPIRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PCBClient(
    private val oldAuthToken: String, // Deprecated
    private val authToken: String,
    private val baseUrl: String,
    private val withLogging: Boolean
) {
    private val instance: Retrofit = build()

    val banApi: BanAPIRequest = instance.create(BanAPIRequest::class.java)
    val authApi: AuthAPIRequest = instance.create(AuthAPIRequest::class.java)
    val balanceApi: BalanceAPIRequest = instance.create(BalanceAPIRequest::class.java)
    val donorApi: DonorAPIRequest = instance.create(DonorAPIRequest::class.java)

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
