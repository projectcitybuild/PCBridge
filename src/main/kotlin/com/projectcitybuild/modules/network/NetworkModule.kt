package com.projectcitybuild.modules.network

import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.network.mojang.client.MojangClient
import com.projectcitybuild.modules.network.pcb.client.PCBClient
import dagger.Module
import dagger.Provides

@Module
class NetworkModule {

    @Provides
    fun provideAPIRequestFactory(config: PlatformConfig): APIRequestFactory {
        val isLoggingEnabled = config.get(PluginConfig.API_IS_LOGGING_ENABLED)

        return APIRequestFactory(
            pcb = PCBClient(
                authToken = config.get(PluginConfig.API_KEY),
                baseUrl = config.get(PluginConfig.API_BASE_URL),
                withLogging = isLoggingEnabled
            ),
            mojang = MojangClient(
                withLogging = isLoggingEnabled
            )
        )
    }
}