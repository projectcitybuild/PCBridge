package com.projectcitybuild.core.infrastructure.network

import com.projectcitybuild.modules.config.ConfigKeys
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.core.infrastructure.network.mojang.client.MojangClient
import com.projectcitybuild.core.infrastructure.network.pcb.client.PCBClient
import dagger.Module
import dagger.Provides

@Module
class NetworkProvider {

    @Provides
    fun provideAPIRequestFactory(config: PlatformConfig): APIRequestFactory {
        val isLoggingEnabled = config.get(ConfigKeys.API_IS_LOGGING_ENABLED)

        return APIRequestFactory(
            pcb = PCBClient(
                authToken = config.get(ConfigKeys.API_KEY),
                baseUrl = config.get(ConfigKeys.API_BASE_URL),
                withLogging = isLoggingEnabled
            ),
            mojang = MojangClient(
                withLogging = isLoggingEnabled
            )
        )
    }
}