package com.projectcitybuild.core.infrastructure.redis

import com.projectcitybuild.modules.config.ConfigKey
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
            hostname = config.get(ConfigKey.REDIS_HOSTNAME),
            port = config.get(ConfigKey.REDIS_PORT),
            username = config.get(ConfigKey.REDIS_USERNAME),
            password = config.get(ConfigKey.REDIS_PASSWORD),
        )
    }
}