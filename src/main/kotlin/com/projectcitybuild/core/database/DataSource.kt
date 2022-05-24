package com.projectcitybuild.core.database

import co.aikar.idb.DatabaseOptions
import co.aikar.idb.HikariPooledDatabase
import co.aikar.idb.PooledDatabaseOptions
import com.projectcitybuild.modules.logger.PlatformLogger
import javax.inject.Singleton

@Singleton
class DataSource(
    private val logger: PlatformLogger,
    private val hostName: String,
    private val port: Int = 3306,
    private val databaseName: String,
    private val username: String,
    private val password: String,
    private val shouldRunMigrations: Boolean,
) {
    class DatabaseNotFoundException : Exception()
    class UndeterminedMigrationVersion : Exception()

    private var database: HikariPooledDatabase? = null

    fun connect() {
        val options = DatabaseOptions.builder()
            .mysql(username, password, databaseName, "$hostName:$port")
            .build()

        database = PooledDatabaseOptions.builder().options(options).createHikariDatabase()

        if (!hasDatabase(databaseName)) {
            throw DatabaseNotFoundException()
        }
        if (shouldRunMigrations) {
            val version = getVersion()
            Migration.executeIfNecessary(database!!, logger, currentVersion = version)
        }
    }

    fun disconnect() {
        database?.close()
    }

    fun database(): HikariPooledDatabase {
        return database ?: throw Exception("Not connected to a database yet")
    }

    private fun getVersion(): Int {
        if (!hasTable("meta")) {
            return 0
        }

        val version = database!!.getFirstColumn<Int>("SELECT `version` FROM `meta` LIMIT 1")
        return version ?: throw UndeterminedMigrationVersion()
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

    private fun hasTable(expectedName: String): Boolean {
        var doesExist = false
        val resultSet = database!!.connection.metaData.getTables(null, null, null, arrayOf("TABLE"))
        while (resultSet.next()) {
            val tableName = resultSet.getString("TABLE_NAME")
            if (tableName == expectedName) {
                doesExist = true
                break
            }
        }
        resultSet.close()

        return doesExist
    }
}
