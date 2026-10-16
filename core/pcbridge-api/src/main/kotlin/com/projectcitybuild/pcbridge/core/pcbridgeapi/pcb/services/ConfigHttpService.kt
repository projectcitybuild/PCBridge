package com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services

import com.projectcitybuild.pcbridge.core.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.RemoteConfigVersion
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.requests.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

class ConfigHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun get(): RemoteConfigVersion = withContext(Dispatchers.IO) {
        responseParser.parse(retrofit.pcb()::getConfig)
    }
}
