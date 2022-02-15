package com.projectcitybuild.core.infrastructure.redis

import com.projectcitybuild.modules.config.ConfigKeys
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
            hostname = config.get(ConfigKeys.REDIS_HOSTNAME),
            port = config.get(ConfigKeys.REDIS_PORT),
            username = config.get(ConfigKeys.REDIS_USERNAME),
            password = config.get(ConfigKeys.REDIS_PASSWORD),
        )
    }
}