package com.projectcitybuild.modules.database

import com.projectcitybuild.entities.migrations.*
import com.projectcitybuild.modules.logger.LoggerProvider
import com.zaxxer.hikari.HikariDataSource
import net.md_5.bungee.api.plugin.Plugin

object Migration {

    private val migrations = arrayOf(
        `20220114_first_time_run`(),
        `20220115_player_configs_warps`(),
    )

    fun executeIfNecessary(
        dataSource: HikariDataSource,
        logger: LoggerProvider,
        plugin: Plugin, // temporary
        currentVersion: Int
    ) {
        var version = currentVersion
        val totalMigrations = migrations.size

        if (version >= totalMigrations) return

        while (version < totalMigrations) {
            val migration = migrations[version]
            logger.info("Running migration ${version + 1}: ${migration.description}")
            migration.execute(dataSource, plugin)
            version++
        }

        if (currentVersion != totalMigrations) {
            updateVersion(dataSource, version)
        }
    }

    private fun updateVersion(dataSource: HikariDataSource, newVersion: Int) {
        val statement = dataSource.connection.prepareStatement(
            "UPDATE meta SET `version`=?"
        ).apply {
            setInt(1, newVersion)
        }
        statement.executeUpdate()
    }
}