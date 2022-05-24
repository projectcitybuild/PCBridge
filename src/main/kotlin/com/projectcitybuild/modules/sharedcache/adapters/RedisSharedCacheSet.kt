package com.projectcitybuild.modules.sharedcache.adapters

import com.projectcitybuild.core.redis.RedisConnection
import com.projectcitybuild.modules.sharedcache.SharedCacheSet
import javax.inject.Inject

class RedisSharedCacheSet @Inject constructor(
    private val redisConnection: RedisConnection,
) : SharedCacheSet {

    override lateinit var key: String

    override fun has(value: String): Boolean {
        return redisConnection.resource().use {
            it.sismember(key, value)
        }
    }

    override fun add(value: String) {
        redisConnection.resource().use {
            it.sadd(key, value)
        }
    }

    override fun add(values: List<String>) {
        redisConnection.resource().use {
            values.forEach { value -> it.sadd(key, value) }
        }
    }

    override fun remove(value: String) {
        redisConnection.resource().use {
            it.srem(key, value)
        }
    }

    override fun removeAll() {
        redisConnection.resource().use {
            it.del(key)
        }
    }

    override fun all(): Set<String> {
        return redisConnection.resource().use {
            it.smembers(key)
        }
    }
}
