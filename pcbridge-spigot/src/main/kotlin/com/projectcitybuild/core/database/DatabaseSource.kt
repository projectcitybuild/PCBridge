package com.projectcitybuild.core.database

import com.projectcitybuild.core.config.PluginConfig

data class DatabaseSource(
    val hostName: String,
    val port: Int = 3306,
    val databaseName: String,
    val username: String,
    val password: String,
) {
    val hostAndPort: String
        get() = "$hostName:$port"

    companion object {
        fun fromConfig(config: PluginConfig) = DatabaseSource(
            hostName = config.database.hostName,
            port = config.database.port,
            databaseName = config.database.name,
            username = config.database.username,
            password = config.database.password,
        )
    }
}