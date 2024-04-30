package com.projectcitybuild.core.database

import co.aikar.idb.DatabaseOptions
import co.aikar.idb.HikariPooledDatabase
import co.aikar.idb.PooledDatabaseOptions
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger

class DatabaseSession(
    private val logger: PlatformLogger,
) {
    class DatabaseNotFoundException : Exception()

    private var database: HikariPooledDatabase? = null

    fun connect(source: DatabaseSource) {
        val options = DatabaseOptions.builder()
            .mysql(source.username, source.password, source.databaseName, source.hostAndPort)
            .build()

        database = PooledDatabaseOptions.builder()
            .options(options)
            .createHikariDatabase()

        if (!hasDatabase(source.databaseName)) {
            throw DatabaseNotFoundException()
        }
        logger.info("Database connection established")
    }

    fun disconnect() {
        database?.close()
        database = null

        logger.info("Database connection closed")
    }

    fun database(): HikariPooledDatabase? {
        if (database == null) {
            logger.warning("Tried to access database without an established connection")
        }
        return database
    }

    private fun hasDatabase(expectedName: String): Boolean {
        var doesExist = false
        val resultSet = database!!.connection.metaData.catalogs
        while (resultSet.next()) {
            val databaseName = resultSet.getString(1)
            if (databaseName == expectedName) {
                doesExist = true
                break
            }
        }
        resultSet.close()

        return doesExist
    }
}