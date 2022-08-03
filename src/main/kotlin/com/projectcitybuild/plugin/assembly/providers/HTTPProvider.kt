package com.projectcitybuild.plugin.assembly.providers

import com.projectcitybuild.core.http.APIRequestFactory
import com.projectcitybuild.core.http.clients.MojangClient
import com.projectcitybuild.core.http.clients.PCBClient
import com.projectcitybuild.modules.config.Config
import com.projectcitybuild.modules.config.ConfigKeys
import dagger.Module
import dagger.Provides

@Module
class HTTPProvider {

    @Provides
    fun provideAPIRequestFactory(config: Config): APIRequestFactory {
        val isLoggingEnabled = config.get(ConfigKeys.apiIsLoggingEnabled)

        return APIRequestFactory(
            pcb = PCBClient(
                oldAuthToken = config.get(ConfigKeys.apiKey),
                authToken = config.get(ConfigKeys.apiToken),
                baseUrl = config.get(ConfigKeys.apiBaseURL),
                withLogging = isLoggingEnabled
            ),
            mojang = MojangClient(
                withLogging = isLoggingEnabled
            )
        )
    }
}
