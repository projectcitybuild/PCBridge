package com.projectcitybuild.entities.migrations

import com.projectcitybuild.modules.logger.LoggerProvider
import com.zaxxer.hikari.HikariDataSource

object Migration {

    fun executeIfNecessary(dataSource: HikariDataSource, logger: LoggerProvider, currentVersion: Int) {
        var version = currentVersion
        val totalMigrations = migrations.size

        if (version >= totalMigrations) return

        while (version < totalMigrations) {
            val migration = migrations[version]
            logger.info("Running migration ${version + 1}: ${migration.first}")
            migration.second(dataSource)
            version++
        }

        if (currentVersion != totalMigrations) {
            updateVersion(dataSource, version)
        }
    }

    private val migrations: Array<Pair<String, (HikariDataSource) -> Unit>> = arrayOf(
        Pair("First-time run") { dataSource ->
            val connection = dataSource.connection

            connection
                .prepareStatement("CREATE TABLE IF NOT EXISTS meta(version INT(64));")
                .executeUpdate()

            connection
                .prepareStatement("INSERT INTO meta VALUES(1);")
                .executeUpdate()
        }
    )

    private fun updateVersion(dataSource: HikariDataSource, newVersion: Int) {
        val statement = dataSource.connection.prepareStatement(
            "UPDATE meta SET `version`=?"
        ).apply {
            setInt(1, newVersion)
        }
        statement.executeUpdate()
    }
}