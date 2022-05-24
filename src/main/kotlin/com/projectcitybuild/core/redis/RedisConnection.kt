package com.projectcitybuild.core.redis

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import javax.inject.Singleton

@Singleton
class RedisConnection(
    private val hostname: String,
    private val port: Int,
    private val username: String,
    private val password: String,
) {
    private lateinit var connectionPool: JedisPool

    fun connect() {
        connectionPool = JedisPool(hostname, port).also { jedis ->
            if (password.isEmpty()) return@also

            jedis.resource.use {
                if (username.isEmpty()) it.auth(password)
                else it.auth(username, password)
            }
        }
        testConnection()
    }

    fun disconnect() {
        connectionPool.close()
    }

    fun resource(): Jedis {
        return connectionPool.resource
    }

    private fun testConnection() {
        resource().use {
            it.set("pcbridge:test", "test")
            it.del("pcbridge:test")
        }
    }
}
