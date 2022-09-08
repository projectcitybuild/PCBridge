package com.projectcitybuild.core.http.clients

import com.projectcitybuild.entities.requests.pcb.AggregateAPIRequest
import com.projectcitybuild.entities.requests.pcb.AuthAPIRequest
import com.projectcitybuild.entities.requests.pcb.BalanceAPIRequest
import com.projectcitybuild.entities.requests.pcb.BanAPIRequest
import com.projectcitybuild.entities.requests.pcb.DonorAPIRequest
import com.projectcitybuild.entities.requests.pcb.TelemetryAPIRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PCBClient(
    private val authToken: String,
    private val baseUrl: String,
    private val withLogging: Boolean
) {
    private val instance: Retrofit = build()

    val aggregateAPI: AggregateAPIRequest = instance.create(AggregateAPIRequest::class.java)
    val banAPI: BanAPIRequest = instance.create(BanAPIRequest::class.java)
    val authAPI: AuthAPIRequest = instance.create(AuthAPIRequest::class.java)
    val balanceAPI: BalanceAPIRequest = instance.create(BalanceAPIRequest::class.java)
    val donorAPI: DonorAPIRequest = instance.create(DonorAPIRequest::class.java)
    val telemetryAPI: TelemetryAPIRequest = instance.create(TelemetryAPIRequest::class.java)

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
                val requestBuilder = request.newBuilder().header("Authorization", "Bearer $authToken")
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
