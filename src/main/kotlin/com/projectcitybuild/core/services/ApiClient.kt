package com.projectcitybuild.core.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {
    companion object {
        val instance: Retrofit by lazy {
            Retrofit.Builder()
                    .baseUrl("https://projectcitybuild.com/api")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
    }
}