package com.projectcitybuild.modules.database

import com.projectcitybuild.entities.migrations.Migration
import com.projectcitybuild.modules.logger.LoggerProvider
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.md_5.bungee.api.plugin.Plugin
import java.sql.Connection

class DataSource(
    private val plugin: Plugin,
    private val logger: LoggerProvider,
    private val hostName: String,
    private val port: Int = 3306,
    private val databaseName: String,
    private val databaseUsername: String,
    private val databasePassword: String,
    private val shouldRunMigrations: Boolean,
) {
    class DatabaseNotFoundException: Exception()
    class UndeterminedMigrationVersion: Exception()

    private lateinit var dataSource: HikariDataSource

    private fun makeConfig() = HikariConfig().apply {
        jdbcUrl = "jdbc:mysql://$hostName:$port/$databaseName"
        username = databaseUsername
        password = databasePassword

        addDataSourceProperty( "cachePrepStmts" , "true" )
        addDataSourceProperty( "prepStmtCacheSize" , "250" )
        addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" )
    }

    fun connect() {
        dataSource = HikariDataSource(makeConfig())

        if (!hasDatabase(databaseName)) {
            throw DatabaseNotFoundException()
        }
        if (shouldRunMigrations) {
            val version = getVersion()
            Migration.executeIfNecessary(dataSource, logger, plugin, currentVersion = version)
        }
    }

    fun close() {
        if (dataSource.connection == null) return

        dataSource.connection.close()
    }

    fun connection(): Connection {
        return dataSource.connection
    }

    private fun getVersion(): Int {
        var version = 0
        if (hasTable("meta")) {
            val statement = dataSource.connection.prepareStatement("SELECT `version` FROM `meta` LIMIT 1")
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                version = resultSet.getInt(1)
            } else {
                throw UndeterminedMigrationVersion()
            }
            resultSet.close()
        }
        return version
    }

    private fun hasDatabase(expectedName: String): Boolean {
        var doesExist = false
        val resultSet = dataSource.connection.metaData.catalogs
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
        val resultSet = dataSource.connection.metaData.getTables(null, null, null , arrayOf("TABLE"))
        while(resultSet.next()) {
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