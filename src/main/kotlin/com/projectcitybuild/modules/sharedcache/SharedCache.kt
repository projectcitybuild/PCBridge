package com.projectcitybuild.modules.sharedcache

import com.projectcitybuild.modules.redis.RedisConnection
import javax.inject.Inject

class SharedCache @Inject constructor(
    private val redis: RedisConnection,
) {
    var prefix: String = "" // TODO: inject this

    private fun getKey(key: String): String {
        return "$prefix.$key"
    }

    fun get(key: String): String? {
        return redis.resource().use {
            it.get(getKey(key))
        }
    }

    fun set(key: String, value: String) {
        return redis.resource().use {
            it.set(getKey(key), value)
        }
    }
}