package com.projectcitybuild.modules.sharedcache

import com.projectcitybuild.modules.redis.RedisConnection
import javax.inject.Inject

class SharedCache @Inject constructor(
    private val redis: RedisConnection,
) {
    fun get(key: String): String? {
        return redis.resource().use {
            it.get(key)
        }
    }

    fun set(key: String, value: String) {
        return redis.resource().use {
            it.set(key, value)
        }
    }
}