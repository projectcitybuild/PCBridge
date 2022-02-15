package com.projectcitybuild.modules.sharedcache

import com.projectcitybuild.modules.config.ConfigKey
import com.projectcitybuild.modules.config.PlatformConfig
import com.projectcitybuild.modules.sharedcache.adapters.FlatFileSharedCacheSet
import com.projectcitybuild.modules.sharedcache.adapters.RedisSharedCacheSet
import dagger.Module
import dagger.Provides

@Module
class SharedCacheSetProvider {

    @Provides
    fun provideSharedCacheSet(
        config: PlatformConfig,
        redisSharedCacheSet: RedisSharedCacheSet,
        flatFileSharedCacheSet: FlatFileSharedCacheSet,
    ): SharedCacheSet {
        val adapter = config.get(ConfigKey.SHARED_CACHE_ADAPTER)

        return when (adapter) {
            "redis" -> redisSharedCacheSet
            "flatfile" -> flatFileSharedCacheSet
            else -> throw Exception("$adapter is not a valid Shared Cache adapter. Must be of [redis, flatfile]")
        }
    }
}