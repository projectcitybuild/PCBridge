package com.projectcitybuild.modules.redis

import com.projectcitybuild.entities.PluginConfig
import com.projectcitybuild.modules.config.PlatformConfig
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RedisProvider {

    @Provides
    @Singleton
    fun provideRedisConnection(config: PlatformConfig): RedisConnection {
        return RedisConnection(
            hostname = config.get(PluginConfig.REDIS_HOSTNAME),
            port = config.get(PluginConfig.REDIS_PORT),
            username = config.get(PluginConfig.REDIS_USERNAME),
            password = config.get(PluginConfig.REDIS_PASSWORD),
        )
    }
}