package com.projectcitybuild.features.afk.repositories

import com.projectcitybuild.modules.redis.RedisConnection
import dagger.Reusable
import java.util.UUID
import javax.inject.Inject

@Reusable
class AFKRepository @Inject constructor(
    private val redisConnection: RedisConnection,
) {
    private val cacheKey = "pcbridge:afk"

    fun isAFK(uuid: UUID): Boolean {
        return redisConnection.resource().use {
            it.sismember(cacheKey, uuid.toString())
        }
    }

    fun add(uuid: UUID) {
        redisConnection.resource().use {
            it.sadd(cacheKey, uuid.toString())
        }
    }

    fun remove(uuid: UUID) {
        redisConnection.resource().use {
            it.srem(cacheKey, uuid.toString())
        }
    }
}