package com.projectcitybuild.core.database

import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

class DatabaseSession(
    private val logger: PlatformLogger,
) {
    private var database: HikariDataSource? = null

    fun connect(source: DatabaseSource) {
        val url = "jdbc:mysql://${source.hostName}:${source.port}/${source.databaseName}"
        logger.debug("Connecting to $url")

        val config = HikariConfig().apply {
            jdbcUrl = url
            username = source.username
            password = source.password

            // See https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
            addDataSourceProperty("cachePrepStmts", true)
            addDataSourceProperty("prepStmtCacheSize", 250)
            addDataSourceProperty("prepStmtCacheSqlLimit", 2048)
            addDataSourceProperty("useServerPrepStmts", true)
            addDataSourceProperty("useLocalSessionState", true)
            addDataSourceProperty("rewriteBatchedStatements", true)
            addDataSourceProperty("cacheResultSetMetadata", true)
            addDataSourceProperty("cacheServerConfiguration", true)
            addDataSourceProperty("elideSetAutoCommits", true)
            addDataSourceProperty("maintainTimeStats", false)
        }
        database = HikariDataSource(config)

        check (hasDatabase(source.databaseName)) {
            "Database not found: ${source.databaseName}"
        }
        logger.info("Database connection established")
    }

    fun disconnect() {
        database?.close()
        database = null

        logger.info("Database connection closed")
    }

    fun <T> connect(action: (Connection) -> T): T {
        if (database == null) {
            logger.warning("Tried to access database before connecting")
        }
        val connection = database?.connection
        checkNotNull(connection)

        return database!!.connection.use(action)
    }

    private fun hasDatabase(expectedName: String): Boolean {
        var found = false

        connect { connection ->
            connection.metaData.catalogs.use { resultSet ->
                while (resultSet.next()) {
                    val databaseName = resultSet.getString(1)
                    if (databaseName == expectedName) {
                        found = true
                        break
                    }
                }
            }
        }
        return found
    }
}
