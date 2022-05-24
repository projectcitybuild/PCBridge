package com.projectcitybuild.core.http

import com.projectcitybuild.core.http.clients.MojangClient
import com.projectcitybuild.core.http.clients.PCBClient
import com.projectcitybuild.modules.config.Config
import dagger.Module
import dagger.Provides

@Module
class NetworkProvider {

    @Provides
    fun provideAPIRequestFactory(config: Config): APIRequestFactory {
        val isLoggingEnabled = config.keys.API_IS_LOGGING_ENABLED

        return APIRequestFactory(
            pcb = PCBClient(
                oldAuthToken = config.keys.API_KEY,
                authToken = config.keys.API_TOKEN,
                baseUrl = config.keys.API_BASE_URL,
                withLogging = isLoggingEnabled
            ),
            mojang = MojangClient(
                withLogging = isLoggingEnabled
            )
        )
    }
}
