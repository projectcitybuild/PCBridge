package com.projectcitybuild.core.infrastructure.network

import com.projectcitybuild.core.infrastructure.network.mojang.client.MojangClient
import com.projectcitybuild.core.infrastructure.network.pcb.client.PCBClient
import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import dagger.Module
import dagger.Provides

@Module
class NetworkProvider {

    @Provides
    fun provideAPIRequestFactory(config: PlatformConfig): APIRequestFactory {
        val isLoggingEnabled = config.get(ConfigKey.API_IS_LOGGING_ENABLED)

        return APIRequestFactory(
            pcb = PCBClient(
                oldAuthToken = config.get(ConfigKey.API_KEY),
                authToken = config.get(ConfigKey.API_TOKEN),
                baseUrl = config.get(ConfigKey.API_BASE_URL),
                withLogging = isLoggingEnabled
            ),
            mojang = MojangClient(
                withLogging = isLoggingEnabled
            )
        )
    }
}
