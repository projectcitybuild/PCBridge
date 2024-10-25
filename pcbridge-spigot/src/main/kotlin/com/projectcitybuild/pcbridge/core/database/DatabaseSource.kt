package com.projectcitybuild.pcbridge.core.database

import com.projectcitybuild.pcbridge.data.LocalConfigKeyValues

data class DatabaseSource(
    val hostName: String,
    val port: Int = 3306,
    val databaseName: String,
    val username: String,
    val password: String,
) {
    companion object {
        fun fromConfig(config: LocalConfigKeyValues) =
            DatabaseSource(
                hostName = config.database.hostName,
                port = config.database.port,
                databaseName = config.database.name,
                username = config.database.username,
                password = config.database.password,
            )
    }
}
