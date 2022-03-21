package com.projectcitybuild.modules.sharedcache.adapters

import com.projectcitybuild.core.infrastructure.redis.RedisConnection
import com.projectcitybuild.modules.sharedcache.SharedCacheSet
import javax.inject.Inject

class RedisSharedCacheSet @Inject constructor(
    private val redisConnection: RedisConnection,
) : SharedCacheSet {

    override lateinit var key: String

    private fun key(subKey: String?): String {
        if (subKey.isNullOrEmpty()) {
            return key
        }
        return "$key:$subKey"
    }

    override fun has(value: String, subKey: String?): Boolean {
        return redisConnection.resource().use {
            it.sismember(key(subKey), value)
        }
    }

    override fun add(value: String, subKey: String?) {
        redisConnection.resource().use {
            it.sadd(key(subKey), value)
        }
    }

    override fun add(values: List<String>, subKey: String?) {
        redisConnection.resource().use {
            values.forEach { value -> it.sadd(key(subKey), value) }
        }
    }

    override fun remove(value: String, subKey: String?) {
        redisConnection.resource().use {
            it.srem(key(subKey), value)
        }
    }

    override fun removeAll(subKey: String?) {
        redisConnection.resource().use {
            it.del(key(subKey))
        }
    }

    override fun all(subKey: String?): Set<String> {
        return redisConnection.resource().use {
            it.smembers(key(subKey))
        }
    }
}
