package com.projectcitybuild.pcbridge.http.services

import com.projectcitybuild.pcbridge.http.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

class ConfigHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun get(): RemoteConfigVersion =
        withContext(Dispatchers.IO) {
            responseParser.parse(retrofit.pcb()::getConfig)
        }
}
