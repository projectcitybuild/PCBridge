package com.projectcitybuild.modules.redis

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
        connectionPool = if (password.isEmpty()) {
            JedisPool(hostname, port)
        } else {
            JedisPool(hostname, port, username, password)
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
            it.set("test", "test")
            it.del("test")
        }
    }
}